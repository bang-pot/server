package com.bangpot.meeting.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.CancelMeetingService;
import com.bangpot.meeting.application.service.CancelMeetingParticipationService;
import com.bangpot.meeting.application.service.CloseMeetingRecruitmentService;
import com.bangpot.meeting.application.service.CompleteMeetingService;
import com.bangpot.meeting.application.service.CreateMeetingService;
import com.bangpot.meeting.application.service.GetMeetingDetailService;
import com.bangpot.meeting.application.service.GetMeetingsService;
import com.bangpot.meeting.application.service.JoinMeetingService;
import com.bangpot.meeting.application.service.MeetingAutomaticTransitionService;
import com.bangpot.meeting.application.service.RecordMeetingResultService;
import com.bangpot.meeting.application.service.ReopenMeetingRecruitmentService;
import com.bangpot.meeting.application.service.UpdateMeetingService;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.bangpot.meeting.application.usecase.CompleteMeetingUseCase;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.application.usecase.RecordMeetingResultUseCase;
import com.bangpot.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

abstract class AbstractMeetingUseCaseServicesTest {

	protected static final Instant NOW = Instant.parse("2026-04-12T10:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected CompletedUserAccessService completedUserAccessService;
	protected InMemoryCrewRepository crewRepository;
	protected InMemoryCrewMemberRepository crewMemberRepository;
	protected InMemoryMeetingRepository meetingRepository;
	protected InMemoryMeetingQueryRepository meetingQueryRepository;
	protected InMemoryMeetingParticipantRepository meetingParticipantRepository;
	protected MutableClock clock;
	protected MeetingAutomaticTransitionService meetingAutomaticTransitionService;
	protected CreateMeetingUseCase createMeetingUseCase;
	protected GetMeetingsUseCase getMeetingsUseCase;
	protected GetMeetingDetailUseCase getMeetingDetailUseCase;
	protected JoinMeetingUseCase joinMeetingUseCase;
	protected CancelMeetingParticipationUseCase cancelMeetingParticipationUseCase;
	protected UpdateMeetingUseCase updateMeetingUseCase;
	protected CloseMeetingRecruitmentUseCase closeMeetingRecruitmentUseCase;
	protected ReopenMeetingRecruitmentUseCase reopenMeetingRecruitmentUseCase;
	protected CancelMeetingUseCase cancelMeetingUseCase;
	protected CompleteMeetingUseCase completeMeetingUseCase;
	protected RecordMeetingResultUseCase recordMeetingResultUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingQueryRepository = new InMemoryMeetingQueryRepository(
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		clock = new MutableClock(NOW);
		meetingAutomaticTransitionService = new MeetingAutomaticTransitionService(
			meetingRepository,
			meetingParticipantRepository,
			clock
		);
		createMeetingUseCase = new CreateMeetingService(completedUserAccessService, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingsUseCase = new GetMeetingsService(
			completedUserAccessService,
			meetingQueryRepository
		);
		getMeetingDetailUseCase = new GetMeetingDetailService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository,
			meetingAutomaticTransitionService
		);
		joinMeetingUseCase = new JoinMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository,
			meetingAutomaticTransitionService
		);
		cancelMeetingParticipationUseCase = new CancelMeetingParticipationService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		updateMeetingUseCase = new UpdateMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
		closeMeetingRecruitmentUseCase = new CloseMeetingRecruitmentService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
		reopenMeetingRecruitmentUseCase = new ReopenMeetingRecruitmentService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
		cancelMeetingUseCase = new CancelMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
		completeMeetingUseCase = new CompleteMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
		recordMeetingResultUseCase = new RecordMeetingResultService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingAutomaticTransitionService
		);
	}

	protected AuthUser fullUser(Long id, String providerId, String nickname) {
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

	protected AuthUser tempUser(Long id, String providerId) {
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

	protected static final class InMemoryAuthUserRepository implements AuthUserRepository {
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

	protected static final class InMemoryCrewRepository implements CrewRepository {
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
		protected int findByIdForShareCallCount;

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
			findByIdForShareCallCount++;
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

	protected static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}


		private final InMemoryAuthUserRepository authUserRepository;

		private InMemoryUserRepository(InMemoryAuthUserRepository authUserRepository) {
			this.authUserRepository = authUserRepository;
		}

		@Override
		public Optional<User> findById(Long userId) {
			return authUserRepository.findById(userId)
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL)
				.map(this::toDomain);
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return authUserRepository.usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return List.of();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			throw new UnsupportedOperationException();
		}

		private User toDomain(AuthUser authUser) {
			return User.create(authUser.getId(), authUser.getNickname());
		}
	}

	protected static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

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
			return findByCrewIdAndUserId(crewId, userId)
				.map(member -> member.getRole() == CrewRole.LEADER)
				.orElse(false);
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
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
				.toList();
		}
	}

	protected static final class InMemoryMeetingRepository implements MeetingRepository {
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
			return meetingsById.values().stream()
				.filter(meeting -> meetingId.equals(meeting.getId()) && crewId.equals(meeting.getCrewId()))
				.findFirst();
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

	protected static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryMeetingRepository meetingRepository;

		private InMemoryMeetingQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryMeetingRepository meetingRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
			this.meetingRepository = meetingRepository;
		}

		@Override
		public Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId) {
			return crewRepository.findById(crewId)
				.map(crew -> MeetingsAccessView.of(
					crew.getId(),
					crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
						.map(CrewMember::getRole)
						.orElse(null)
				));
		}

		@Override
		public MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			List<MeetingsView.Item> allItems = meetingRepository.findAllByCrewId(crewId).stream()
				.map(meeting -> MeetingsView.Item.of(
					meeting.getId(),
					meeting.getTitle(),
					meeting.getThemeName(),
					meeting.getPlace(),
					meeting.getMeetingDate(),
					meeting.getMeetingTime(),
					meeting.getStatus().name(),
					meeting.getResult().name(),
					meeting.getCapacity()
				))
				.toList();
			int fromIndex = Math.min(page * size, allItems.size());
			int toIndex = Math.min(fromIndex + size, allItems.size());
			return MeetingsView.of(
				allItems.subList(fromIndex, toIndex),
				MeetingsView.Page.of(page, size, toIndex < allItems.size())
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MyCalendarView.of(List.of(), 0);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(
			Long userId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MyCreatedMeetingsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(
			Long userId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MyJoinedMeetingsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			return com.bangpot.meeting.domain.view.UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			return com.bangpot.meeting.domain.view.CrewScheduleView.of(List.of());
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
		}

		@Override
		public Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds) {
			return Map.of();
		}
	}

	protected static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {
		private final Map<Long, MeetingParticipant> participantsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
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
		public int leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(
			Long crewId,
			Long userId,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public long countByMeetingId(Long meetingId) {
			return participantsById.values().stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()))
				.filter(participant -> participant.getStatus().representsJoined())
				.count();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participantsById.remove(participant.getId());
		}
	}

	protected static final class MutableClock extends Clock {
		private Instant instant;

		private MutableClock(Instant instant) {
			this.instant = instant;
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("UTC");
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}


