package com.bangpot.user.application;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
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
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CheckNicknameAvailabilityService;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.application.service.GetMyCreatedMeetingsService;
import com.bangpot.user.application.service.GetMyCalendarService;
import com.bangpot.user.application.service.GetMyFavoriteThemesService;
import com.bangpot.user.application.service.GetMyFavoriteThemesSummaryService;
import com.bangpot.user.application.service.GetMyMeetingLogsService;
import com.bangpot.user.application.service.GetMyJoinedMeetingsService;
import com.bangpot.user.application.service.GetMyCrewsService;
import com.bangpot.user.application.service.CancelMyPendingCrewJoinRequestService;
import com.bangpot.user.application.service.GetMyPendingCrewsService;
import com.bangpot.user.application.service.GetMyProfileService;
import com.bangpot.user.application.service.GetMyWithdrawalCheckService;
import com.bangpot.user.application.service.SearchUsersService;
import com.bangpot.user.application.service.WithdrawMyAccountService;
import com.bangpot.user.application.service.UpdateMyProfileService;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
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

abstract class AbstractUserApplicationServiceTest {

	protected static final Instant BASE_TIME = Instant.parse("2026-04-14T00:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected InMemoryProfileHubReadRepository profileHubReadRepository;
	protected InMemoryMeetingRepository meetingRepository;
	protected InMemoryCrewRepository crewRepository;
	protected InMemoryCrewMemberRepository crewMemberRepository;
	protected InMemoryCreatedMeetingReadRepository createdMeetingReadRepository;
	protected InMemoryCalendarReadRepository calendarReadRepository;
	protected InMemoryMyFavoriteThemeReadRepository myFavoriteThemeReadRepository;
	protected InMemoryFavoriteThemeSummaryReadRepository favoriteThemeSummaryReadRepository;
	protected InMemoryMyMeetingLogReadRepository myMeetingLogReadRepository;
	protected InMemoryJoinedMeetingReadRepository joinedMeetingReadRepository;
	protected InMemoryMyCrewReadRepository myCrewReadRepository;
	protected InMemoryPendingCrewReadRepository pendingCrewReadRepository;
	protected InMemoryUserSearchReadRepository userSearchReadRepository;
	protected InMemoryUserWithdrawalRepository userWithdrawalRepository;
	protected CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	protected CancelMyPendingCrewJoinRequestUseCase cancelMyPendingCrewJoinRequestUseCase;
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
		profileHubReadRepository = new InMemoryProfileHubReadRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		crewRepository = new InMemoryCrewRepository(crewMemberRepository);
		createdMeetingReadRepository = new InMemoryCreatedMeetingReadRepository();
		calendarReadRepository = new InMemoryCalendarReadRepository();
		myFavoriteThemeReadRepository = new InMemoryMyFavoriteThemeReadRepository();
		favoriteThemeSummaryReadRepository = new InMemoryFavoriteThemeSummaryReadRepository();
		myMeetingLogReadRepository = new InMemoryMyMeetingLogReadRepository();
		joinedMeetingReadRepository = new InMemoryJoinedMeetingReadRepository();
		myCrewReadRepository = new InMemoryMyCrewReadRepository();
		pendingCrewReadRepository = new InMemoryPendingCrewReadRepository();
		userSearchReadRepository = new InMemoryUserSearchReadRepository();
		userWithdrawalRepository = new InMemoryUserWithdrawalRepository();
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		getMyProfileUseCase = new GetMyProfileService(userRepository, meetingRepository, crewRepository);
		getMyCreatedMeetingsUseCase = new GetMyCreatedMeetingsService(
			authUserRepository,
			userRepository,
			createdMeetingReadRepository
		);
		getMyCalendarUseCase = new GetMyCalendarService(
			authUserRepository,
			userRepository,
			calendarReadRepository
		);
		getMyFavoriteThemesUseCase = new GetMyFavoriteThemesService(
			authUserRepository,
			userRepository,
			myFavoriteThemeReadRepository
		);
		getMyFavoriteThemesSummaryUseCase = new GetMyFavoriteThemesSummaryService(
			authUserRepository,
			userRepository,
			favoriteThemeSummaryReadRepository
		);
		getMyMeetingLogsUseCase = new GetMyMeetingLogsService(
			authUserRepository,
			userRepository,
			myMeetingLogReadRepository
		);
		getMyJoinedMeetingsUseCase = new GetMyJoinedMeetingsService(
			authUserRepository,
			userRepository,
			joinedMeetingReadRepository
		);
		getMyCrewsUseCase = new GetMyCrewsService(
			authUserRepository,
			userRepository,
			myCrewReadRepository
		);
		getMyPendingCrewsUseCase = new GetMyPendingCrewsService(
			authUserRepository,
			userRepository,
			pendingCrewReadRepository
		);
		getMyWithdrawalCheckUseCase = new GetMyWithdrawalCheckService(
			userRepository,
			crewRepository
		);
		searchUsersUseCase = new SearchUsersService(
			authUserRepository,
			userRepository,
			userSearchReadRepository
		);
		withdrawMyAccountUseCase = new WithdrawMyAccountService(
			authUserRepository,
			userRepository,
			getMyWithdrawalCheckUseCase,
			userWithdrawalRepository,
			Clock.fixed(BASE_TIME, ZoneOffset.UTC)
		);
		cancelMyPendingCrewJoinRequestUseCase = new CancelMyPendingCrewJoinRequestService(
			authUserRepository,
			userRepository,
			pendingCrewReadRepository
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
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}

		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
			withdrawnUserIds.add(userId);
		}

		private final java.util.Set<Long> withdrawnUserIds = new java.util.HashSet<>();
	}

	protected static final class InMemoryProfileHubReadRepository
		implements com.bangpot.user.application.port.ProfileHubReadRepository {
		private final Map<Long, Counts> countsByUserId = new HashMap<>();

		@Override
		public Counts loadCounts(Long userId) {
			return countsByUserId.getOrDefault(userId, Counts.of(0L, 0L, 0L, 0L));
		}

		void putCounts(Long userId, long createdMeetingsCount, long joinedMeetingsCount, long myCrewsCount, long pendingCrewsCount) {
			countsByUserId.put(
				userId,
				Counts.of(createdMeetingsCount, joinedMeetingsCount, myCrewsCount, pendingCrewsCount)
			);
		}
	}

	protected static final class InMemoryMeetingRepository implements MeetingRepository {
		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private final Map<Long, Long> createdCountsByUserId = new HashMap<>();
		private final Map<Long, Long> joinedCountsByUserId = new HashMap<>();

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
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> crewId.equals(meeting.getCrewId()));
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
	}

	protected static final class InMemoryCrewRepository implements CrewRepository {
		private final Map<Long, Crew> crewsById = new HashMap<>();
		private final Map<Long, Long> activeCountsByUserId = new HashMap<>();
		private final Map<Long, Long> pendingCountsByUserId = new HashMap<>();
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private long sequence = 1L;

		private InMemoryCrewRepository(InMemoryCrewMemberRepository crewMemberRepository) {
			this.crewMemberRepository = crewMemberRepository;
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
		public List<Crew> findActiveByMemberUserId(Long userId) {
			return crewsById.values().stream()
				.filter(crew -> crewMemberRepository.findAllByUserId(userId).stream()
					.anyMatch(crewMember -> crew.getId().equals(crewMember.getCrewId())))
				.sorted(java.util.Comparator.comparing(Crew::getName).thenComparing(Crew::getId))
				.toList();
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			return activeCountsByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			return pendingCountsByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}

		void putCounts(Long userId, long activeCount, long pendingCount) {
			activeCountsByUserId.put(userId, activeCount);
			pendingCountsByUserId.put(userId, pendingCount);
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

	protected static final class InMemoryCreatedMeetingReadRepository
		implements com.bangpot.user.application.port.CreatedMeetingReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}
	}

	protected static final class InMemoryCalendarReadRepository
		implements com.bangpot.user.application.port.CalendarReadRepository {
		private final Map<Long, View> viewsByUserId = new HashMap<>();

		@Override
		public View load(Long userId) {
			return viewsByUserId.getOrDefault(userId, View.of(List.of(), 0));
		}

		void putView(Long userId, View view) {
			viewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryFavoriteThemeSummaryReadRepository
		implements com.bangpot.user.application.port.FavoriteThemeSummaryReadRepository {
		private final Map<Long, View> viewsByUserId = new HashMap<>();

		@Override
		public View load(Long userId, int limit) {
			return viewsByUserId.getOrDefault(userId, View.of(List.of(), 0L));
		}

		void putView(Long userId, View view) {
			viewsByUserId.put(userId, view);
		}
	}

	protected static final class InMemoryMyFavoriteThemeReadRepository
		implements com.bangpot.user.application.port.MyFavoriteThemeReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}
	}

	protected static final class InMemoryMyMeetingLogReadRepository
		implements com.bangpot.user.application.port.MyMeetingLogReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}
	}

	protected static final class InMemoryJoinedMeetingReadRepository
		implements com.bangpot.user.application.port.JoinedMeetingReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}
	}

	protected static final class InMemoryMyCrewReadRepository
		implements com.bangpot.user.application.port.MyCrewReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}
	}

	protected static final class InMemoryPendingCrewReadRepository
		implements com.bangpot.user.application.port.PendingCrewReadRepository {
		private final Map<Long, SearchResult> resultsByUserId = new HashMap<>();
		private final Map<String, CancelResult> cancelResultsByOwnerAndRequestId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultsByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}

		@Override
		public CancelResult cancel(Long userId, Long joinRequestId) {
			CancelResult cancelResult = cancelResultsByOwnerAndRequestId.get(cancelKey(userId, joinRequestId));
			if (cancelResult == null) {
				throw new CrewJoinRequestNotFoundException(joinRequestId);
			}
			return cancelResult;
		}

		void putResult(Long userId, SearchResult result) {
			resultsByUserId.put(userId, result);
		}

		void putCancelable(Long userId, CancelResult cancelResult) {
			cancelResultsByOwnerAndRequestId.put(cancelKey(userId, cancelResult.joinRequestId()), cancelResult);
		}

		private String cancelKey(Long userId, Long joinRequestId) {
			return userId + ":" + joinRequestId;
		}
	}

	protected static final class InMemoryUserSearchReadRepository
		implements com.bangpot.user.application.port.UserSearchReadRepository {
		private final Map<String, List<Item>> itemsByKeyword = new HashMap<>();
		private int searchCount;

		@Override
		public List<Item> search(String keyword, int size) {
			searchCount++;
			return itemsByKeyword.getOrDefault(keyword, List.of()).stream()
				.limit(size)
				.toList();
		}

		void putResult(String keyword, List<Item> items) {
			itemsByKeyword.put(keyword, items);
		}

		int getSearchCount() {
			return searchCount;
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
