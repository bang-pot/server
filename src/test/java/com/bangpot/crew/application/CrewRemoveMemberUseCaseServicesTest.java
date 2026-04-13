package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.Instant;
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
import com.bangpot.crew.application.exception.CrewRemoveMemberTargetNotAllowedException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewHubService;
import com.bangpot.crew.application.service.RemoveCrewMemberService;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewRemoveMemberUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-14T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingRepository meetingRepository;
	private InMemoryMeetingParticipantRepository meetingParticipantRepository;
	private RemoveCrewMemberUseCase removeCrewMemberUseCase;
	private GetCrewHubUseCase getCrewHubUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		removeCrewMemberUseCase = new RemoveCrewMemberService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		getCrewHubUseCase = new GetCrewHubService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			new InMemoryCrewJoinRequestRepository()
		);
	}

	@Test
	void removesCurrentMemberAndRevokesCrewAccess() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser member = fullUser(2L, "member-provider", "member");
		authUserRepository.save(leader);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		RemoveCrewMemberUseCase.Result result = removeCrewMemberUseCase.handle(
			RemoveCrewMemberUseCase.Command.of(crew.getId(), leader.getId(), member.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.removedUserId()).isEqualTo(member.getId());
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), member.getId())).isFalse();
		assertThat(crewMemberRepository.findAnyByCrewIdAndUserId(crew.getId(), member.getId())).get()
			.extracting(CrewMember::getStatus)
			.isEqualTo(CrewMemberStatus.REMOVED);
		assertThatThrownBy(() -> getCrewHubUseCase.handle(GetCrewHubUseCase.Query.of(crew.getId(), member.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void cancelsHostedUnfinishedMeetingsAndMarksJoinedParticipationsLeft() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		AuthUser anotherHost = fullUser(3L, "another-provider", "another");
		authUserRepository.save(leader);
		authUserRepository.save(target);
		authUserRepository.save(anotherHost);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), target.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), anotherHost.getId()));

		Meeting hostedRecruiting = meetingRepository.save(Meeting.create(
			crew.getId(), target.getId(), "Theme A", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		Meeting hostedClosed = meetingRepository.save(Meeting.create(
			crew.getId(), target.getId(), "Theme B", "Hongdae", "2026-04-21", "20:00", 4, null, null, null, null
		));
		hostedClosed.closeRecruitment();
		meetingRepository.save(hostedClosed);

		Meeting joinedMeeting = meetingRepository.save(Meeting.create(
			crew.getId(), anotherHost.getId(), "Theme C", "Kondae", "2026-04-22", "21:00", 4, null, null, null, null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(joinedMeeting.getId(), target.getId()));

		removeCrewMemberUseCase.handle(RemoveCrewMemberUseCase.Command.of(crew.getId(), leader.getId(), target.getId()));

		assertThat(meetingRepository.findById(hostedRecruiting.getId())).get()
			.extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.CANCELED);
		assertThat(meetingRepository.findById(hostedClosed.getId())).get()
			.extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.CANCELED);
		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(joinedMeeting.getId(), target.getId())).get()
			.extracting(MeetingParticipant::getStatus)
			.isEqualTo(MeetingParticipationStatus.LEFT);
	}

	@Test
	void rejectsRemoveWhenCurrentUserIsNotLeader() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(member);
		authUserRepository.save(target);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), target.getId()));

		assertThatThrownBy(() -> removeCrewMemberUseCase.handle(
			RemoveCrewMemberUseCase.Command.of(crew.getId(), member.getId(), target.getId())
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsRemoveWhenTargetIsNotCurrentMember() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser outsider = fullUser(2L, "outsider-provider", "outsider");
		authUserRepository.save(leader);
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> removeCrewMemberUseCase.handle(
			RemoveCrewMemberUseCase.Command.of(crew.getId(), leader.getId(), outsider.getId())
		)).isInstanceOf(CrewRemoveMemberTargetNotAllowedException.class);
	}

	private AuthUser fullUser(Long id, String providerId, String nickname) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			nickname,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
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

		private final InMemoryAuthUserRepository authUserRepository;

		private InMemoryUserRepository(InMemoryAuthUserRepository authUserRepository) {
			this.authUserRepository = authUserRepository;
		}

		@Override
		public Optional<User> findById(Long userId) {
			return authUserRepository.findById(userId)
				.map(authUser -> User.rehydrate(authUser.getId(), authUser.getNickname(), authUser.getStatus() == AuthUserStatus.FULL));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return false;
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return List.of();
		}

		@Override
		public User save(User user) {
			throw new UnsupportedOperationException();
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
			return List.of();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {

		private final Map<Long, CrewMember> membersById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			membersById.put(crewMember.getId(), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.anyMatch(member ->
					crewId.equals(member.getCrewId()) &&
						userId.equals(member.getUserId()) &&
						member.getRole() == CrewRole.LEADER
				);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()) && member.isActive())
				.findFirst();
		}

		@Override
		public Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()))
				.findFirst();
		}

		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
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
				.sorted((left, right) -> left.getId().compareTo(right.getId()))
				.toList();
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return Optional.ofNullable(meetingsById.get(meetingId))
				.filter(meeting -> crewId.equals(meeting.getCrewId()));
		}
	}

	private static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {

		private final Map<Long, MeetingParticipant> participantsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
			if (participant.getCreatedAt() == null) {
				setField(participant, "createdAt", NOW);
			}
			setField(participant, "updatedAt", NOW);
			participantsById.put(participant.getId(), participant);
			return participant;
		}

		@Override
		public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
			return participantsById.values().stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()) && userId.equals(participant.getUserId()))
				.findFirst();
		}

		@Override
		public long countByMeetingId(Long meetingId) {
			return participantsById.values().stream()
				.filter(participant ->
					meetingId.equals(participant.getMeetingId()) &&
						participant.getStatus().representsJoined()
				)
				.count();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participantsById.remove(participant.getId());
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {

		@Override
		public CrewJoinRequest save(CrewJoinRequest request) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
			return false;
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
			return Optional.empty();
		}
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
