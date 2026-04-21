package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewScheduleService;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.MeetingAutomaticTransitionService;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewScheduleUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-20T12:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingRepository meetingRepository;
	private InMemoryMeetingParticipantRepository meetingParticipantRepository;
	private GetCrewScheduleUseCase getCrewScheduleUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		MeetingAutomaticTransitionService meetingAutomaticTransitionService = new MeetingAutomaticTransitionService(
			meetingRepository,
			meetingParticipantRepository,
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
		getCrewScheduleUseCase = new GetCrewScheduleService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository,
			meetingAutomaticTransitionService
		);
	}

	@Test
	void returnsCrewScheduleForJoinedMemberIncludingRecruitingCompletedAndCanceled() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.rehydrate(7L, "requester"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), requester.getId(), CrewRole.MEMBER));

		Meeting recruiting = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Recruiting Meeting", "Theme A", "Hongdae", "2026-04-20", "23:00", 4, null, null, null
		));
		Meeting completed = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Completed Meeting", "Theme B", "Gangnam", "2026-04-21", "18:00", 4, null, null, null
		));
		completed.closeRecruitment();
		completed.complete();
		meetingRepository.save(completed);
		Meeting canceled = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Canceled Meeting", "Theme C", "Seongsu", "2026-04-22", "17:00", 4, null, null, null
		));
		canceled.cancel();
		meetingRepository.save(canceled);
		Meeting outsidePeriod = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Outside Meeting", "Theme D", "Jamsil", "2026-04-25", "16:00", 4, null, null, null
		));

		meetingParticipantRepository.save(MeetingParticipant.join(recruiting.getId(), requester.getId()));
		meetingParticipantRepository.save(
			MeetingParticipant.rehydrate(null, completed.getId(), requester.getId(), MeetingParticipationStatus.APPROVED, null, null)
		);
		meetingParticipantRepository.save(MeetingParticipant.join(canceled.getId(), requester.getId()));
		meetingParticipantRepository.save(MeetingParticipant.join(outsidePeriod.getId(), requester.getId()));

		GetCrewScheduleUseCase.Result result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(crew.getId(), requester.getId(), "2026-04-20", "2026-04-22")
		);

		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::meetingId)
			.containsExactly(recruiting.getId(), completed.getId(), canceled.getId());
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::themeName)
			.containsExactly("Theme A", "Theme B", "Theme C");
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::meetingStatus)
			.containsExactly("RECRUITING", "COMPLETED", "CANCELED");
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::recruitmentStatus)
			.containsExactly("OPEN", "CLOSED", "CLOSED");
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::place)
			.containsExactly("Hongdae", "Gangnam", "Seongsu");
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::participantCount)
			.containsExactly(2L, 2L, 2L);
		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::isCanceled)
			.containsExactly(false, false, true);
	}

	@Test
	void sortsCrewScheduleByDateTimeAndMeetingIdAscending() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.rehydrate(7L, "requester"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), requester.getId(), CrewRole.MEMBER));

		Meeting first = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "First", "Theme A", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting second = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Second", "Theme B", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting third = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Third", "Theme C", "Hongdae", "2026-04-20", "19:00", 4, null, null, null
		));

		meetingParticipantRepository.save(MeetingParticipant.join(first.getId(), requester.getId()));
		meetingParticipantRepository.save(MeetingParticipant.join(second.getId(), requester.getId()));
		meetingParticipantRepository.save(MeetingParticipant.join(third.getId(), requester.getId()));

		GetCrewScheduleUseCase.Result result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(crew.getId(), requester.getId(), "2026-04-20", "2026-04-20")
		);

		assertThat(result.items()).extracting(GetCrewScheduleUseCase.Item::meetingId)
			.containsExactly(first.getId(), second.getId(), third.getId());
	}

	@Test
	void appliesAutomaticMeetingTransitionBeforeBuildingSchedule() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.rehydrate(7L, "requester"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), requester.getId(), CrewRole.MEMBER));

		Meeting staleRecruiting = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Stale Recruiting", "Theme A", "Hongdae", "2026-04-20", "09:00", 4, null, null, null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(staleRecruiting.getId(), requester.getId()));

		GetCrewScheduleUseCase.Result result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(crew.getId(), requester.getId(), "2026-04-20", "2026-04-20")
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingStatus()).isEqualTo("COMPLETED");
		assertThat(meetingRepository.findById(staleRecruiting.getId())).get()
			.extracting(Meeting::getStatus)
			.isEqualTo(com.bangpot.meeting.domain.MeetingStatus.COMPLETED);
	}

	@Test
	void rejectsCrewScheduleForNonMember() {
		AuthUser outsider = fullAuthUser(99L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		userRepository.save(User.rehydrate(99L, "outsider"));
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(crew.getId(), outsider.getId(), "2026-04-20", "2026-04-22")
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewScheduleForTempUser() {
		AuthUser tempUser = tempAuthUser(88L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), tempUser.getId(), CrewRole.MEMBER));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(crew.getId(), tempUser.getId(), "2026-04-20", "2026-04-22")
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewScheduleForUnknownCrew() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.rehydrate(7L, "requester"));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(999L, requester.getId(), "2026-04-20", "2026-04-22")
		)).isInstanceOf(CrewNotFoundException.class);
	}

	private AuthUser fullAuthUser(Long id, String providerId, String nickname) {
		userRepository.save(User.rehydrate(id, nickname));
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private AuthUser tempAuthUser(Long id, String providerId) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.TEMP,
			null,
			null,
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private CrewMember crewMember(Long id, Long crewId, Long userId, CrewRole role) {
		CrewMember member = role == CrewRole.LEADER
			? CrewMember.createLeader(crewId, userId)
			: CrewMember.createMember(crewId, userId);
		member.assignId(id);
		return member;
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {

		private final Map<Long, AuthUser> usersById = new HashMap<>();

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return usersById.values().stream()
				.filter(user -> user.getProvider() == provider && providerId.equals(user.getProviderId()))
				.findFirst();
		}

		@Override
		public AuthUser save(AuthUser user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {

		private final Map<Long, User> usersById = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalized = nickname == null ? null : nickname.trim().toLowerCase();
			return usersById.values().stream()
				.filter(user -> normalized == null || user.getNickname().toLowerCase().contains(normalized))
				.toList();
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {

		private final Map<Long, Crew> crewsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crewsById.values().stream().anyMatch(crew -> name.equals(crew.getName()));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crewsById.put(crew.getId(), crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.ofNullable(crewsById.get(crewId));
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {

		private final List<CrewMember> members = new ArrayList<>();

		@Override
		public CrewMember save(CrewMember crewMember) {
			members.removeIf(existing -> existing.getId().equals(crewMember.getId()));
			members.add(crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId)
				.map(member -> member.getRole() == CrewRole.LEADER)
				.orElse(false);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return members.stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()))
				.findFirst();
		}

		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {

		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Meeting save(Meeting meeting) {
			if (meeting.getId() == null) {
				meeting.assignId(sequence++);
			}
			meetingsById.put(meeting.getId(), meeting);
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return meetingsById.values().stream()
				.filter(meeting -> crewId.equals(meeting.getCrewId()))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.toList();
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> crewId.equals(meeting.getCrewId()));
		}
	}

	private static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {

		private final List<MeetingParticipant> participants = new ArrayList<>();
		private long sequence = 1L;

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
			participants.removeIf(existing -> existing.getId().equals(participant.getId()));
			participants.add(participant);
			return participant;
		}

		@Override
		public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
			return participants.stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()) && userId.equals(participant.getUserId()))
				.findFirst();
		}

		@Override
		public long countByMeetingId(Long meetingId) {
			return participants.stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()))
				.filter(participant -> participant.getStatus().representsJoined())
				.count();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participants.removeIf(existing -> existing.getId().equals(participant.getId()));
		}
	}
}
