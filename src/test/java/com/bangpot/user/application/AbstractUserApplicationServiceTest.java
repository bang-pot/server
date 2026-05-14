package com.bangpot.user.application;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bangpot.auth.application.port.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.port.ThemeFavoriteQueryRepository;
import com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView;
import com.bangpot.explore.domain.view.MyFavoriteThemesView;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.MyPendingCrewsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.service.CheckNicknameAvailabilityService;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.application.service.GetMyCreatedMeetingsService;
import com.bangpot.user.application.service.GetMyCalendarService;
import com.bangpot.user.application.service.GetMyFavoriteThemesService;
import com.bangpot.user.application.service.GetMyFavoriteThemesSummaryService;
import com.bangpot.user.application.service.GetMyMeetingLogsService;
import com.bangpot.user.application.service.GetMyJoinedMeetingsService;
import com.bangpot.user.application.service.GetMyCrewsService;
import com.bangpot.user.application.service.GetMyPendingCrewsService;
import com.bangpot.user.application.service.GetMyProfileService;
import com.bangpot.user.application.service.GetMyWithdrawalCheckService;
import com.bangpot.user.application.service.SearchUsersService;
import com.bangpot.user.application.service.WithdrawMyAccountService;
import com.bangpot.user.application.service.UpdateMyProfileService;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.UserWithdrawal;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.view.UserSearchView;

abstract class AbstractUserApplicationServiceTest {

	protected static final Instant BASE_TIME = Instant.parse("2026-04-14T00:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected InMemoryUserQueryRepository userQueryRepository;
	protected InMemoryMeetingQueryRepository meetingQueryRepository;
	protected InMemoryCrewQueryRepository crewQueryRepository;
	protected InMemoryMeetingRepository meetingRepository;
	protected InMemoryCrewRepository crewRepository;
	protected InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	protected InMemoryCrewJoinRequestQueryRepository crewJoinRequestQueryRepository;
	protected InMemoryCrewMemberRepository crewMemberRepository;
	protected InMemoryThemeFavoriteRepository themeFavoriteRepository;
	protected InMemoryThemeFavoriteQueryRepository themeFavoriteQueryRepository;
	protected InMemoryMeetingLogRepository meetingLogRepository;
	protected InMemoryMeetingLogQueryRepository meetingLogQueryRepository;
	protected InMemoryUserWithdrawalRepository userWithdrawalRepository;
	protected CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	protected GetMyProfileUseCase getMyProfileUseCase;
	protected GetMyCreatedMeetingsUseCase getMyCreatedMeetingsUseCase;
	protected GetMyCalendarUseCase getMyCalendarUseCase;
	protected GetMyFavoriteThemesUseCase getMyFavoriteThemesUseCase;
	protected GetMyFavoriteThemesSummaryUseCase getMyFavoriteThemesSummaryUseCase;
	protected GetMyMeetingLogsUseCase getMyMeetingLogsUseCase;
	protected GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;
	protected GetMyCrewsUseCase getMyCrewsUseCase;
	protected GetMyPendingCrewsUseCase getMyPendingCrewsUseCase;
	protected GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;
	protected SearchUsersUseCase searchUsersUseCase;
	protected WithdrawMyAccountUseCase withdrawMyAccountUseCase;
	protected UpdateMyProfileUseCase updateMyProfileUseCase;
	protected CompletedUserAccessService completedUserAccessService;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		crewRepository = new InMemoryCrewRepository(crewMemberRepository, userRepository);
		meetingQueryRepository = new InMemoryMeetingQueryRepository(meetingRepository);
		crewQueryRepository = new InMemoryCrewQueryRepository(crewRepository);
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		crewJoinRequestQueryRepository = new InMemoryCrewJoinRequestQueryRepository(crewJoinRequestRepository);
		themeFavoriteRepository = new InMemoryThemeFavoriteRepository();
		themeFavoriteQueryRepository = new InMemoryThemeFavoriteQueryRepository(themeFavoriteRepository);
		meetingLogRepository = new InMemoryMeetingLogRepository();
		meetingLogQueryRepository = new InMemoryMeetingLogQueryRepository(meetingLogRepository);
		userWithdrawalRepository = new InMemoryUserWithdrawalRepository();
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		userQueryRepository = new InMemoryUserQueryRepository(userRepository);
		getMyProfileUseCase = new GetMyProfileService(userQueryRepository, meetingQueryRepository, crewQueryRepository);
		getMyCreatedMeetingsUseCase = new GetMyCreatedMeetingsService(
			userQueryRepository,
			meetingQueryRepository
		);
		getMyCalendarUseCase = new GetMyCalendarService(
			userQueryRepository,
			meetingQueryRepository
		);
		getMyFavoriteThemesUseCase = new GetMyFavoriteThemesService(
			userQueryRepository,
			themeFavoriteQueryRepository
		);
		getMyFavoriteThemesSummaryUseCase = new GetMyFavoriteThemesSummaryService(
			userQueryRepository,
			themeFavoriteQueryRepository
		);
		getMyMeetingLogsUseCase = new GetMyMeetingLogsService(
			userQueryRepository,
			meetingLogQueryRepository
		);
		getMyJoinedMeetingsUseCase = new GetMyJoinedMeetingsService(
			userQueryRepository,
			meetingQueryRepository
		);
		getMyCrewsUseCase = new GetMyCrewsService(
			userQueryRepository,
			crewQueryRepository
		);
		getMyPendingCrewsUseCase = new GetMyPendingCrewsService(
			userQueryRepository,
			crewJoinRequestQueryRepository
		);
		getMyWithdrawalCheckUseCase = new GetMyWithdrawalCheckService(
			userQueryRepository,
			crewQueryRepository
		);
		searchUsersUseCase = new SearchUsersService(meetingQueryRepository, userQueryRepository);
		withdrawMyAccountUseCase = new WithdrawMyAccountService(
			authUserRepository,
			userRepository,
			userWithdrawalRepository,
			getMyWithdrawalCheckUseCase,
			Clock.fixed(BASE_TIME, ZoneOffset.UTC)
		);
		updateMyProfileUseCase = new UpdateMyProfileService(userRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
	}

	protected AuthUser fullUser(Long id, String nickname) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			"provider-" + id,
			AuthUserStatus.FULL,
			nickname,
			RequiredTermsAgreement.of("2026-04-14", BASE_TIME),
			null,
			BASE_TIME,
			BASE_TIME
		);
	}

	protected AuthUser tempUser(Long id) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			"provider-" + id,
			AuthUserStatus.TEMP,
			null,
			null,
			null,
			BASE_TIME,
			BASE_TIME
		);
	}

	protected static final class InMemoryAuthUserRepository implements AuthUserRepository {
		private final Map<Long, AuthUser> authUsers = new HashMap<>();

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(authUsers.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return authUsers.values().stream()
				.filter(user -> user.getProvider() == provider && providerId.equals(user.getProviderId()))
				.findFirst();
		}

		@Override
		public AuthUser save(AuthUser user) {
			authUsers.put(user.getId(), user);
			return user;
		}

		@Override
		public void deleteById(Long userId) {
			authUsers.remove(userId);
		}
	}

	protected static final class InMemoryUserRepository implements UserRepository {
		private final Map<Long, User> users = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(users.get(userId))
				.filter(user -> !withdrawnUserIds.contains(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return users.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return users.values().stream()
				.filter(user -> user.getNickname().contains(nickname))
				.toList();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		public UserSearchView searchByNickname(String nickname, int page, int size) {
			searchCount++;
			lastSearchPage = page;
			lastSearchSize = size;
			List<UserSearchView.Item> allItems = searchItemsByKeyword.getOrDefault(nickname, List.of());
			List<UserSearchView.Item> items = allItems.stream()
				.skip((long)page * size)
				.limit(size)
				.toList();
			long totalElements = allItems.size();
			int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
			return UserSearchView.of(items, UserSearchView.Page.of(page, size, totalElements, totalPages));
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			if (withdrawnUserIds.contains(userId)) {
				return false;
			}
			User user = users.get(userId);
			if (user == null) {
				return false;
			}
			user.updateNickname(nickname);
			return true;
		}

		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
			withdrawnUserIds.add(userId);
		}

		void putSearchResult(String keyword, List<UserSearchView.Item> items) {
			searchItemsByKeyword.put(keyword, items);
		}

		int getSearchCount() {
			return searchCount;
		}

		int getLastSearchSize() {
			return lastSearchSize;
		}

		int getLastSearchPage() {
			return lastSearchPage;
		}

		private final Map<String, List<UserSearchView.Item>> searchItemsByKeyword = new HashMap<>();
		private int searchCount;
		private int lastSearchPage;
		private int lastSearchSize;
		private final java.util.Set<Long> withdrawnUserIds = new java.util.HashSet<>();
	}

	protected static final class InMemoryUserQueryRepository implements UserQueryRepository {
		private final InMemoryUserRepository userRepository;

		private InMemoryUserQueryRepository(InMemoryUserRepository userRepository) {
			this.userRepository = userRepository;
		}

		@Override
		public com.bangpot.user.domain.view.UserProfileView findMyProfileUserViewByUserId(Long userId) {
			return userRepository.findById(userId)
				.map(user -> com.bangpot.user.domain.view.UserProfileView.of(
					user.getId(),
					user.getNickname(),
					null
				))
				.orElse(null);
		}

		@Override
		public boolean existsCompletedUser(Long userId) {
			return userRepository.findById(userId).isPresent();
		}

		@Override
		public UserSearchView searchUsersByNickname(String nickname, int page, int size) {
			return userRepository.searchByNickname(nickname, page, size);
		}
	}

	protected static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {
		private final InMemoryMeetingRepository meetingRepository;

		private InMemoryMeetingQueryRepository(InMemoryMeetingRepository meetingRepository) {
			this.meetingRepository = meetingRepository;
		}

		@Override
		public MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return meetingRepository.findMyCalendarViewByUserId(userId);
		}

		@Override
		public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return meetingRepository.findMyCreatedMeetingsViewByHostUserId(userId, page, size);
		}

		@Override
		public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return meetingRepository.findMyJoinedMeetingsViewByUserId(userId, page, size);
		}

		@Override
		public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(Long userId, int limit, String currentDate, String currentTime) {
			return UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingActivityRecordView findActivityRecordViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MeetingActivityRecordView.empty();
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewMeetingGalleryView findCrewMeetingGalleryView(
			Long crewId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.CrewMeetingGalleryView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.CrewMeetingGalleryView.Page.of(page, size, false)
			);
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.List<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(
			Long meetingId
		) {
			return java.util.List.of();
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
			return meetingRepository.countCreatedByHostUserId(userId);
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return meetingRepository.countJoinedByUserId(userId);
		}

		@Override
		public Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds) {
			return userIds.stream()
				.distinct()
				.filter(completedCountsByUserId::containsKey)
				.collect(java.util.stream.Collectors.toMap(
					java.util.function.Function.identity(),
					completedCountsByUserId::get
				));
		}

		void putCompletedCount(Long userId, int count) {
			completedCountsByUserId.put(userId, count);
		}

		private final Map<Long, Integer> completedCountsByUserId = new HashMap<>();
	}

	protected static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public com.bangpot.crew.domain.view.ExploreCrewCardsView findExploreCrewCardsView(
			String keyword,
			com.bangpot.crew.domain.ExploreCrewSort sort,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
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

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		private final InMemoryCrewRepository crewRepository;

		private InMemoryCrewQueryRepository(InMemoryCrewRepository crewRepository) {
			this.crewRepository = crewRepository;
		}


		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}		@Override
		public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return crewRepository.findMyCrewsViewByMemberUserId(userId, page, size);
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			return crewRepository.countActiveByMemberUserId(userId);
		}

		@Override
		public PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
			return PublicCrewPreviewView.of(List.of());
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			return crewRepository.countActiveByMemberUserId(userId);
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			return crewRepository.countPendingPublicByUserId(userId);
		}

		@Override
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			return com.bangpot.crew.domain.view.MeetingCreateCrewsView.of(List.of());
		}

		@Override
		public List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			return crewRepository.findActiveByMemberUserId(userId).stream()
				.map(crew -> com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew.of(
					crew.getId(),
					crew.getName()
				))
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

		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private final Map<Long, Long> createdCountsByUserId = new HashMap<>();
		private final Map<Long, Long> joinedCountsByUserId = new HashMap<>();
		private final Map<Long, MyCalendarView> calendarViewsByUserId = new HashMap<>();
		private final Map<Long, MyCreatedMeetingsView> createdMeetingsViewsByUserId = new HashMap<>();
		private final Map<Long, MyJoinedMeetingsView> joinedMeetingsViewsByUserId = new HashMap<>();

		@Override
		public Meeting save(Meeting meeting) {
			meetingsById.put(meeting.getId(), meeting);
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<Meeting> findRecruitmentCloseTargets(java.time.LocalDateTime now, int limit) {
			return List.of();
		}

		@Override
		public List<Meeting> findCompletionTargets(java.time.LocalDateTime completionCutoff, int limit) {
			return List.of();
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
		public Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId) {
			return findByIdAndCrewId(meetingId, crewId);
		}

		@Override
		public int recordResultIfNotRecorded(
			Long meetingId,
			Long crewId,
			Long hostUserId,
			com.bangpot.meeting.domain.MeetingResult result,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return createdMeetingsViewsByUserId.getOrDefault(
				userId,
				MyCreatedMeetingsView.of(List.of(), MyCreatedMeetingsView.Page.of(page, size, false))
			);
		}

		@Override
		public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return joinedMeetingsViewsByUserId.getOrDefault(
				userId,
				MyJoinedMeetingsView.of(List.of(), MyJoinedMeetingsView.Page.of(page, size, false))
			);
		}

		@Override
		public MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return calendarViewsByUserId.getOrDefault(userId, MyCalendarView.of(List.of(), 0));
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return createdCountsByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return joinedCountsByUserId.getOrDefault(userId, 0L);
		}

		void putCounts(Long userId, long createdMeetingsCount, long joinedMeetingsCount) {
			createdCountsByUserId.put(userId, createdMeetingsCount);
			joinedCountsByUserId.put(userId, joinedMeetingsCount);
		}

		void putCalendarView(Long userId, MyCalendarView view) {
			calendarViewsByUserId.put(userId, view);
		}

		void putCreatedMeetingsView(Long userId, MyCreatedMeetingsView view) {
			createdMeetingsViewsByUserId.put(userId, view);
		}

		void putJoinedMeetingsView(Long userId, MyJoinedMeetingsView view) {
			joinedMeetingsViewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}

		private final Map<Long, Crew> crewsById = new HashMap<>();
		private final Map<Long, Long> activeCountsByUserId = new HashMap<>();
		private final Map<Long, Long> pendingCountsByUserId = new HashMap<>();
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryUserRepository userRepository;
		private long sequence = 1L;

		private InMemoryCrewRepository(
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryUserRepository userRepository
		) {
			this.crewMemberRepository = crewMemberRepository;
			this.userRepository = userRepository;
		}

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
			return crewsById.values().stream()
				.filter(crew -> crewMemberRepository.findAllByUserId(userId).stream()
					.anyMatch(crewMember -> crew.getId().equals(crewMember.getCrewId())))
				.sorted(java.util.Comparator.comparing(Crew::getName).thenComparing(Crew::getId))
				.toList();
		}

		@Override
		public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			List<MyCrewsView.Item> items = findActiveByMemberUserId(userId).stream()
				.skip((long) page * size)
				.limit(size + 1L)
				.map(crew -> MyCrewsView.Item.of(
					crew.getId(),
					crew.getName(),
					crew.getVisibility(),
					findLeaderNickname(crew.getId()),
					crew.getImageUrl()
				))
				.toList();
			boolean hasNext = items.size() > size;
			List<MyCrewsView.Item> pageItems = hasNext ? items.subList(0, size) : items;
			return MyCrewsView.of(pageItems, MyCrewsView.Page.of(page, size, hasNext));
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			return activeCountsByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			return pendingCountsByUserId.getOrDefault(userId, 0L);
		}

		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}

		void putCounts(Long userId, long activeCount, long pendingCount) {
			activeCountsByUserId.put(userId, activeCount);
			pendingCountsByUserId.put(userId, pendingCount);
		}

		private String findLeaderNickname(Long crewId) {
			return crewMemberRepository.findAllByCrewId(crewId).stream()
				.filter(crewMember -> crewMember.getRole() == CrewRole.LEADER)
				.findFirst()
				.map(CrewMember::getUserId)
				.flatMap(userRepository::findById)
				.map(User::getNickname)
				.orElse(null);
		}
	}

	protected static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		private final Map<String, CrewMember> crewMembersByCrewAndUser = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			crewMembersByCrewAndUser.put(key(crewMember.getCrewId(), crewMember.getUserId()), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId)
				.filter(crewMember -> crewMember.getRole() == com.bangpot.crew.domain.CrewRole.LEADER)
				.isPresent();
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return Optional.ofNullable(crewMembersByCrewAndUser.get(key(crewId, userId)))
				.filter(CrewMember::isActive);
		}

		@Override
		public Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return Optional.ofNullable(crewMembersByCrewAndUser.get(key(crewId, userId)));
		}
		@Override
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return crewMembersByCrewAndUser.values().stream()
				.filter(CrewMember::isActive)
				.filter(crewMember -> crewId.equals(crewMember.getCrewId()))
				.toList();
		}

		@Override
		public List<CrewMember> findAllByUserId(Long userId) {
			return crewMembersByCrewAndUser.values().stream()
				.filter(CrewMember::isActive)
				.filter(crewMember -> userId.equals(crewMember.getUserId()))
				.sorted(java.util.Comparator.comparing(CrewMember::getCrewId))
				.toList();
		}

		private String key(Long crewId, Long userId) {
			return crewId + ":" + userId;
		}
	}

	protected static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		private final Map<Long, MyPendingCrewsView> pendingViewsByUserId = new HashMap<>();
		private final Map<Long, CrewJoinRequest> storedJoinRequests = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
			if (crewJoinRequest.getId() == null) {
				crewJoinRequest.assignId(sequence++);
			}
			storedJoinRequests.put(crewJoinRequest.getId(), crewJoinRequest);
			return crewJoinRequest;
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

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId) {
			return findPendingByIdAndCrewId(requestId, crewId);
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			return storedJoinRequests.values().stream()
				.filter(joinRequest -> requestId.equals(joinRequest.getId()))
				.filter(joinRequest -> userId.equals(joinRequest.getUserId()))
				.filter(joinRequest -> joinRequest.getStatus() == CrewJoinRequestStatus.PENDING)
				.findFirst();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndUserIdForUpdate(Long requestId, Long userId) {
			return findPendingByIdAndUserId(requestId, userId);
		}

		void putPendingView(Long userId, MyPendingCrewsView view) {
			pendingViewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryCrewJoinRequestQueryRepository implements CrewJoinRequestQueryRepository {
		private final InMemoryCrewJoinRequestRepository crewJoinRequestRepository;

		private InMemoryCrewJoinRequestQueryRepository(InMemoryCrewJoinRequestRepository crewJoinRequestRepository) {
			this.crewJoinRequestRepository = crewJoinRequestRepository;
		}

		@Override
		public MyPendingCrewsView findMyPendingCrewsViewByUserId(Long userId, int page, int size) {
			return crewJoinRequestRepository.pendingViewsByUserId.getOrDefault(
				userId,
				MyPendingCrewsView.of(List.of(), MyPendingCrewsView.Page.of(page, size, false))
			);
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView>
			findManagementAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewJoinRequestsView findCrewJoinRequestsViewByCrewId(
			Long crewId,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.PendingCrewJoinRequestsView findPendingCrewJoinRequestsViewByCrewId(
			Long crewId,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}
	}

	protected static final class InMemoryThemeFavoriteRepository implements ThemeFavoriteRepository {
		private final Map<Long, MyFavoriteThemesView> viewsByUserId = new HashMap<>();

		@Override
		public boolean create(Long userId, Long themeId, Instant createdAt) {
			return false;
		}

		@Override
		public boolean delete(Long userId, Long themeId) {
			return false;
		}

		@Override
		public java.util.Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
			return java.util.Set.of();
		}

		void putView(Long userId, MyFavoriteThemesView view) {
			viewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryThemeFavoriteQueryRepository implements ThemeFavoriteQueryRepository {
		private final InMemoryThemeFavoriteRepository themeFavoriteRepository;

		private InMemoryThemeFavoriteQueryRepository(InMemoryThemeFavoriteRepository themeFavoriteRepository) {
			this.themeFavoriteRepository = themeFavoriteRepository;
		}

		@Override
		public MyFavoriteThemesView findMyFavoriteThemesViewByUserId(Long userId, int page, int size) {
			return themeFavoriteRepository.viewsByUserId.getOrDefault(
				userId,
				MyFavoriteThemesView.of(List.of(), MyFavoriteThemesView.Page.of(page, size, false))
			);
		}

		@Override
		public MyFavoriteThemesSummaryView findMyFavoriteThemesSummaryViewByUserId(Long userId, int limit) {
			MyFavoriteThemesView view = themeFavoriteRepository.viewsByUserId.getOrDefault(
				userId,
				MyFavoriteThemesView.of(List.of(), MyFavoriteThemesView.Page.of(0, limit, false))
			);
			return MyFavoriteThemesSummaryView.of(
				view.items().stream().limit(limit).map(item -> MyFavoriteThemesSummaryView.Item.of(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionName(),
					item.thumbnailUrl(),
					item.favoriteCount(),
					item.isFavorite()
				)).toList(),
				(long) view.items().size(),
				view.items().size() > limit
			);
		}
	}

	protected static final class InMemoryMeetingLogRepository implements MeetingLogRepository {
		private final Map<Long, MeetingLog> logsById = new HashMap<>();
		private final Map<Long, MyMeetingLogsView> viewsByUserId = new HashMap<>();

		@Override
		public MeetingLog save(MeetingLog log) {
			logsById.put(log.getId(), log);
			return log;
		}

		@Override
		public Optional<MeetingLog> findById(Long logId) {
			return Optional.ofNullable(logsById.get(logId));
		}

		@Override
		public Optional<MeetingLog> findByIdForUpdate(Long logId) {
			return findById(logId);
		}

		@Override
		public Optional<MeetingLog> findActiveLogInCrewForUpdate(Long crewId, Long logId) {
			return findByIdForUpdate(logId);
		}

		@Override
		public Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return logsById.values().stream()
				.filter(log -> meetingId.equals(log.getMeetingId()) && authorUserId.equals(log.getAuthorUserId()))
				.findFirst();
		}

		@Override
		public boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return findByMeetingIdAndAuthorUserId(meetingId, authorUserId).isPresent();
		}

		@Override
		public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return false;
		}

		void putView(Long userId, MyMeetingLogsView view) {
			viewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryMeetingLogQueryRepository implements MeetingLogQueryRepository {
		private final InMemoryMeetingLogRepository meetingLogRepository;

		private InMemoryMeetingLogQueryRepository(InMemoryMeetingLogRepository meetingLogRepository) {
			this.meetingLogRepository = meetingLogRepository;
		}

		@Override
		public MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(Long userId, int page, int size) {
			return meetingLogRepository.viewsByUserId.getOrDefault(
				userId,
				MyMeetingLogsView.of(List.of(), MyMeetingLogsView.Page.of(page, size, false))
			);
		}

		@Override
		public boolean existsMeetingById(Long meetingId) {
			return false;
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MyMeetingLogView> findMyMeetingLogView(
			Long meetingId,
			Long authorUserId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingLogDetailView> findMeetingLogDetailView(
			Long crewId,
			Long logId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return false;
		}
	}

	protected static final class InMemoryUserWithdrawalRepository
		implements com.bangpot.user.application.port.UserWithdrawalRepository {
		private final Map<Long, UserWithdrawal> withdrawalsByUserId = new HashMap<>();

		@Override
		public UserWithdrawal save(UserWithdrawal userWithdrawal) {
			withdrawalsByUserId.put(userWithdrawal.getUserId(), userWithdrawal);
			return userWithdrawal;
		}

		UserWithdrawal findByUserId(Long userId) {
			return withdrawalsByUserId.get(userId);
		}
	}
}
