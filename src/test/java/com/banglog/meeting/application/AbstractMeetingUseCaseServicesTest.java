package com.banglog.meeting.application;

import java.lang.reflect.Field;
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

import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.domain.AuthUser;
import com.banglog.auth.domain.AuthUserStatus;
import com.banglog.auth.domain.RequiredTermsAgreement;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.service.CancelMeetingService;
import com.banglog.meeting.application.service.CancelMeetingParticipationService;
import com.banglog.meeting.application.service.CloseMeetingRecruitmentService;
import com.banglog.meeting.application.service.CompleteMeetingService;
import com.banglog.meeting.application.service.CreateMeetingService;
import com.banglog.meeting.application.service.GetMeetingDetailService;
import com.banglog.meeting.application.service.GetMeetingsService;
import com.banglog.meeting.application.service.JoinMeetingService;
import com.banglog.meeting.application.service.MeetingAccessService;
import com.banglog.meeting.application.service.MeetingCompletionService;
import com.banglog.meeting.application.service.MeetingRecruitmentCloseService;
import com.banglog.meeting.application.service.ReopenMeetingRecruitmentService;
import com.banglog.meeting.application.service.UpdateMeetingService;
import com.banglog.scheduler.meeting.MeetingCompletionScheduler;
import com.banglog.scheduler.meeting.MeetingRecruitmentCloseScheduler;
import com.banglog.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingUseCase;
import com.banglog.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.CompleteMeetingUseCase;
import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingsAccessView;
import com.banglog.meeting.domain.view.MeetingsView;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.service.CompletedUserAccessService;
import com.banglog.user.domain.User;

abstract class AbstractMeetingUseCaseServicesTest {

	protected static final Instant NOW = Instant.parse("2026-04-12T10:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected CompletedUserAccessService completedUserAccessService;
	protected InMemoryCrewRepository crewRepository;
	protected InMemoryCrewMemberRepository crewMemberRepository;
	protected InMemoryMeetingRepository meetingRepository;
	protected InMemoryMeetingQueryRepository meetingQueryRepository;
	protected MeetingAccessService meetingAccessService;
	protected InMemoryMeetingParticipantRepository meetingParticipantRepository;
	protected MutableClock clock;
	protected MeetingRecruitmentCloseService meetingRecruitmentCloseService;
	protected MeetingCompletionService meetingCompletionService;
	protected MeetingRecruitmentCloseScheduler meetingRecruitmentCloseScheduler;
	protected MeetingCompletionScheduler meetingCompletionScheduler;
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

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		meetingQueryRepository = new InMemoryMeetingQueryRepository(
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		meetingAccessService = new MeetingAccessService(meetingQueryRepository);
		clock = new MutableClock(NOW);
		meetingRecruitmentCloseService = new MeetingRecruitmentCloseService(
			meetingRepository,
			meetingParticipantRepository,
			clock
		);
		meetingCompletionService = new MeetingCompletionService(meetingRepository, clock);
		meetingRecruitmentCloseScheduler = new MeetingRecruitmentCloseScheduler(meetingRecruitmentCloseService);
		meetingCompletionScheduler = new MeetingCompletionScheduler(meetingCompletionService);
		createMeetingUseCase = new CreateMeetingService(completedUserAccessService, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingsUseCase = new GetMeetingsService(
			completedUserAccessService,
			meetingAccessService,
			meetingQueryRepository
		);
		getMeetingDetailUseCase = new GetMeetingDetailService(
			completedUserAccessService,
			meetingAccessService,
			meetingQueryRepository
		);
		joinMeetingUseCase = new JoinMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository,
			meetingRecruitmentCloseService
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
			meetingRecruitmentCloseService
		);
		closeMeetingRecruitmentUseCase = new CloseMeetingRecruitmentService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		reopenMeetingRecruitmentUseCase = new ReopenMeetingRecruitmentService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository,
			clock
		);
		cancelMeetingUseCase = new CancelMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		completeMeetingUseCase = new CompleteMeetingService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
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
		public java.util.Optional<com.banglog.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}

		@Override
		public com.banglog.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.banglog.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.banglog.crew.domain.view.MyCrewsView.Page.of(page, size, false)
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
		public java.util.List<com.banglog.user.domain.User> findAllCompletedUsers() {
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
		public java.util.List<com.banglog.crew.domain.CrewMember> findAllByUserId(Long userId) {
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
		public com.banglog.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.banglog.meeting.domain.view.MyCalendarView.of(java.util.List.of(), 0);
		}

		@Override
		public com.banglog.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return com.banglog.meeting.domain.view.MyJoinedMeetingsView.of(
				java.util.List.of(),
				com.banglog.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.banglog.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return com.banglog.meeting.domain.view.MyCreatedMeetingsView.of(
				java.util.List.of(),
				com.banglog.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}

		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private long sequence = 1L;
		private boolean findByIdAndCrewIdForUpdateCalled;

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
		public List<Meeting> findRecruitmentCloseTargets(java.time.LocalDateTime now, int limit) {
			return meetingsById.values().stream()
				.filter(meeting -> meeting.getStatus() == MeetingStatus.RECRUITING)
				.filter(meeting -> !startAt(meeting).isAfter(now))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.limit(limit)
				.toList();
		}

		@Override
		public List<Meeting> findCompletionTargets(java.time.LocalDateTime completionCutoff, int limit) {
			return meetingsById.values().stream()
				.filter(meeting -> meeting.getStatus() == MeetingStatus.RECRUITMENT_CLOSED)
				.filter(meeting -> !startAt(meeting).isAfter(completionCutoff))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.limit(limit)
				.toList();
		}

		private java.time.LocalDateTime startAt(Meeting meeting) {
			return java.time.LocalDate.parse(meeting.getMeetingDate()).atTime(java.time.LocalTime.parse(meeting.getMeetingTime()));
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
		public Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId) {
			findByIdAndCrewIdForUpdateCalled = true;
			return findByIdAndCrewId(meetingId, crewId);
		}

		void resetLockTracking() {
			findByIdAndCrewIdForUpdateCalled = false;
		}

		boolean findByIdAndCrewIdForUpdateCalled() {
			return findByIdAndCrewIdForUpdateCalled;
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

	private static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	protected static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryMeetingRepository meetingRepository;
		private final InMemoryMeetingParticipantRepository meetingParticipantRepository;

		private InMemoryMeetingQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryMeetingRepository meetingRepository,
			InMemoryMeetingParticipantRepository meetingParticipantRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
			this.meetingRepository = meetingRepository;
			this.meetingParticipantRepository = meetingParticipantRepository;
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
					1L,
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
		public com.banglog.meeting.domain.view.CrewMeetingGalleryView findCrewMeetingGalleryView(
			Long crewId,
			int page,
			int size
		) {
			return com.banglog.meeting.domain.view.CrewMeetingGalleryView.of(
				List.of(),
				com.banglog.meeting.domain.view.CrewMeetingGalleryView.Page.of(page, size, false)
			);
		}

		@Override
		public java.util.Optional<com.banglog.meeting.domain.view.CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public List<com.banglog.meeting.domain.view.CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(
			Long meetingId
		) {
			return List.of();
		}

		@Override
		public Optional<MeetingDetailView> findMeetingDetailView(
			Long crewId,
			Long meetingId,
			Long userId
		) {
			return meetingRepository.findByIdAndCrewId(meetingId, crewId)
				.map(meeting -> MeetingDetailView.of(
					meeting.getId(),
					meeting.getCrewId(),
					meeting.getHostUserId(),
					meeting.getTitle(),
					meeting.getThemeName(),
					meeting.getPlace(),
					meeting.getMeetingDate(),
					meeting.getMeetingTime(),
					meeting.getCapacity(),
					meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1,
					meeting.getTotalCost(),
					meeting.getContactLink(),
					meeting.getDescription(),
					meeting.getStatus().name(),
					meeting.getHostUserId().equals(userId) || hasJoined(meeting.getId(), userId)
						? "JOINED"
						: "NOT_JOINED"
				));
		}

		private boolean hasJoined(Long meetingId, Long userId) {
			return meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
				.map(participant -> participant.getStatus().representsJoined())
				.orElse(false);
		}

		@Override
		public com.banglog.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.banglog.meeting.domain.view.MyCalendarView.of(List.of(), 0);
		}

		@Override
		public com.banglog.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(
			Long userId,
			int page,
			int size
		) {
			return com.banglog.meeting.domain.view.MyCreatedMeetingsView.of(
				List.of(),
				com.banglog.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.banglog.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(
			Long userId,
			int page,
			int size
		) {
			return com.banglog.meeting.domain.view.MyJoinedMeetingsView.of(
				List.of(),
				com.banglog.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.banglog.meeting.domain.view.UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			return com.banglog.meeting.domain.view.UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public com.banglog.meeting.domain.view.MeetingActivityRecordView findActivityRecordViewByUserId(Long userId) {
			return com.banglog.meeting.domain.view.MeetingActivityRecordView.empty();
		}

		@Override
		public com.banglog.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			return com.banglog.meeting.domain.view.CrewScheduleView.of(List.of());
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
		public int leaveInactiveCrewMemberParticipations(
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

		void setInstant(Instant instant) {
			this.instant = instant;
		}
	}
}


