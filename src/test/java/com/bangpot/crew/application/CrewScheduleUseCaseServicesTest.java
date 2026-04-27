package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
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
import com.bangpot.crew.application.exception.CrewScheduleRequestValidationException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewScheduleService;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewMemberAccessView;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.CrewScheduleView;
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
	private InMemoryCrewQueryRepository crewQueryRepository;
	private InMemoryMeetingQueryRepository meetingQueryRepository;
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
		crewQueryRepository = new InMemoryCrewQueryRepository(crewRepository, crewMemberRepository);
		meetingQueryRepository = new InMemoryMeetingQueryRepository(
			meetingRepository,
			meetingParticipantRepository
		);
		getCrewScheduleUseCase = new GetCrewScheduleService(
			completedUserAccessService,
			crewQueryRepository,
			meetingQueryRepository
		);
	}

	@Test
	void returnsCrewScheduleForJoinedMemberIncludingRecruitingCompletedAndCanceled() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.create(7L, "requester"));

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

		CrewScheduleView result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				crew.getId(),
				requester.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-22")
			)
		);

		assertThat(result.items()).extracting(CrewScheduleView.Item::meetingId)
			.containsExactly(recruiting.getId(), completed.getId(), canceled.getId());
		assertThat(result.items()).extracting(CrewScheduleView.Item::themeName)
			.containsExactly("Theme A", "Theme B", "Theme C");
		assertThat(result.items()).extracting(CrewScheduleView.Item::meetingStatus)
			.containsExactly("RECRUITING", "COMPLETED", "CANCELED");
		assertThat(result.items()).extracting(CrewScheduleView.Item::recruitmentStatus)
			.containsExactly("OPEN", "CLOSED", "CLOSED");
		assertThat(result.items()).extracting(CrewScheduleView.Item::place)
			.containsExactly("Hongdae", "Gangnam", "Seongsu");
		assertThat(result.items()).extracting(CrewScheduleView.Item::participantCount)
			.containsExactly(2L, 2L, 2L);
		assertThat(result.items()).extracting(CrewScheduleView.Item::isCanceled)
			.containsExactly(false, false, true);
	}

	@Test
	void sortsCrewScheduleByDateTimeAndMeetingIdAscending() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.create(7L, "requester"));

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

		CrewScheduleView result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				crew.getId(),
				requester.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-20")
			)
		);

		assertThat(result.items()).extracting(CrewScheduleView.Item::meetingId)
			.containsExactly(first.getId(), second.getId(), third.getId());
	}

	@Test
	void returnsPersistedMeetingStatusWithoutMutatingDuringRead() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.create(7L, "requester"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), requester.getId(), CrewRole.MEMBER));

		Meeting staleRecruiting = meetingRepository.save(Meeting.create(
			crew.getId(), 8L, "Stale Recruiting", "Theme A", "Hongdae", "2026-04-20", "09:00", 4, null, null, null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(staleRecruiting.getId(), requester.getId()));

		CrewScheduleView result = getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				crew.getId(),
				requester.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-20")
			)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingStatus()).isEqualTo("RECRUITING");
		assertThat(meetingRepository.findById(staleRecruiting.getId())).get()
			.extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}

	@Test
	void rejectsCrewScheduleForNonMember() {
		AuthUser outsider = fullAuthUser(99L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		userRepository.save(User.create(99L, "outsider"));
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				crew.getId(),
				outsider.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-22")
			)
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewScheduleForTempUser() {
		AuthUser tempUser = tempAuthUser(88L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(1L, crew.getId(), tempUser.getId(), CrewRole.MEMBER));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				crew.getId(),
				tempUser.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-22")
			)
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewScheduleForUnknownCrew() {
		AuthUser requester = fullAuthUser(7L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.create(7L, "requester"));

		assertThatThrownBy(() -> getCrewScheduleUseCase.handle(
			GetCrewScheduleUseCase.Query.of(
				999L,
				requester.getId(),
				LocalDate.parse("2026-04-20"),
				LocalDate.parse("2026-04-22")
			)
		)).isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void rejectsCrewScheduleQueryWhenDateRangeIsTooLarge() {
		assertThatThrownBy(() -> GetCrewScheduleUseCase.Query.of(
			1L,
			7L,
			LocalDate.parse("2026-01-01"),
			LocalDate.parse("2027-01-02")
		)).isInstanceOf(CrewScheduleRequestValidationException.class);
	}

	private AuthUser fullAuthUser(Long id, String providerId, String nickname) {
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

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;

		private InMemoryCrewQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			return crewRepository.findById(crewId)
				.filter(Crew::isActive)
				.map(crew -> CrewMemberAccessView.of(
					crewMemberRepository.findByCrewIdAndUserId(crew.getId(), userId)
						.filter(CrewMember::isActive)
						.map(CrewMember::getRole)
						.orElse(null)
				));
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew>
			findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private static final List<MeetingStatus> INCLUDED_STATUSES = List.of(
			MeetingStatus.RECRUITING,
			MeetingStatus.RECRUITMENT_CLOSED,
			MeetingStatus.COMPLETED,
			MeetingStatus.CANCELED
		);

		private final InMemoryMeetingRepository meetingRepository;
		private final InMemoryMeetingParticipantRepository meetingParticipantRepository;

		private InMemoryMeetingQueryRepository(
			InMemoryMeetingRepository meetingRepository,
			InMemoryMeetingParticipantRepository meetingParticipantRepository
		) {
			this.meetingRepository = meetingRepository;
			this.meetingParticipantRepository = meetingParticipantRepository;
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(
			Long userId,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(
			Long userId,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to) {
			return CrewScheduleView.of(meetingRepository.findAllByCrewId(crewId).stream()
				.filter(meeting -> INCLUDED_STATUSES.contains(meeting.getStatus()))
				.filter(meeting -> !LocalDate.parse(meeting.getMeetingDate()).isBefore(from))
				.filter(meeting -> !LocalDate.parse(meeting.getMeetingDate()).isAfter(to))
				.map(meeting -> CrewScheduleView.Item.of(
					meeting.getId(),
					meeting.getThemeName(),
					meeting.getMeetingDate(),
					meeting.getMeetingTime(),
					meeting.getStatus().name(),
					meeting.getStatus() == MeetingStatus.RECRUITING ? "OPEN" : "CLOSED",
					meeting.getPlace(),
					meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1L,
					meeting.getStatus() == MeetingStatus.CANCELED
				))
				.toList());
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingDetailView> findMeetingDetailView(
			Long crewId,
			Long meetingId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<Long, Integer> countCompletedByUserIds(java.util.Collection<Long> userIds) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {
		@Override
		public void deleteById(Long userId) {
		}


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
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}


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
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}

		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}


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
		public Optional<Crew> findByIdForUpdate(Long crewId) {
			return findById(crewId);
		}

		@Override
		public Optional<Crew> findByIdForShare(Long crewId) {
			return findById(crewId);
		}

		@Override
		public List<Crew> findActiveByMemberUserId(Long userId) {
			return List.of();
		}



				@Override
		public long countActiveByMemberUserId(Long userId) {
			return 0L;
		}
		@Override
		public long countPendingPublicByUserId(Long userId) {
			return 0L;
		}
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId);
		}


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
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {
		@Override
		public boolean existsUnfinishedByCrewId(Long crewId) {
			return false;
		}

		@Override
		public boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId) {
			return false;
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MyCalendarView.of(java.util.List.of(), 0);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyJoinedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyCreatedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}


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
		public int cancelUnfinishedByCrewIdAndHostUserId(
			Long crewId,
			Long hostUserId,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> crewId.equals(meeting.getCrewId()));
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
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
		public int leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(
			Long crewId,
			Long userId,
			java.time.Instant updatedAt
		) {
			return 0;
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


