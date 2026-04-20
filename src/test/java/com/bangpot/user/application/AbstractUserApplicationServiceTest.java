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
import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
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
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.UserWithdrawal;
import com.bangpot.user.domain.User;

abstract class AbstractUserApplicationServiceTest {

	protected static final Instant BASE_TIME = Instant.parse("2026-04-14T00:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected InMemoryProfileHubReadRepository profileHubReadRepository;
	protected InMemoryCreatedMeetingReadRepository createdMeetingReadRepository;
	protected InMemoryCalendarReadRepository calendarReadRepository;
	protected InMemoryMyFavoriteThemeReadRepository myFavoriteThemeReadRepository;
	protected InMemoryFavoriteThemeSummaryReadRepository favoriteThemeSummaryReadRepository;
	protected InMemoryMyMeetingLogReadRepository myMeetingLogReadRepository;
	protected InMemoryJoinedMeetingReadRepository joinedMeetingReadRepository;
	protected InMemoryMyCrewReadRepository myCrewReadRepository;
	protected InMemoryPendingCrewReadRepository pendingCrewReadRepository;
	protected InMemoryWithdrawalCheckReadRepository withdrawalCheckReadRepository;
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
	protected WithdrawMyAccountUseCase withdrawMyAccountUseCase;
	protected UpdateMyProfileUseCase updateMyProfileUseCase;
	protected CompletedUserAccessService completedUserAccessService;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		profileHubReadRepository = new InMemoryProfileHubReadRepository();
		createdMeetingReadRepository = new InMemoryCreatedMeetingReadRepository();
		calendarReadRepository = new InMemoryCalendarReadRepository();
		myFavoriteThemeReadRepository = new InMemoryMyFavoriteThemeReadRepository();
		favoriteThemeSummaryReadRepository = new InMemoryFavoriteThemeSummaryReadRepository();
		myMeetingLogReadRepository = new InMemoryMyMeetingLogReadRepository();
		joinedMeetingReadRepository = new InMemoryJoinedMeetingReadRepository();
		myCrewReadRepository = new InMemoryMyCrewReadRepository();
		pendingCrewReadRepository = new InMemoryPendingCrewReadRepository();
		withdrawalCheckReadRepository = new InMemoryWithdrawalCheckReadRepository();
		userWithdrawalRepository = new InMemoryUserWithdrawalRepository();
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		getMyProfileUseCase = new GetMyProfileService(authUserRepository, userRepository, profileHubReadRepository);
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
			authUserRepository,
			userRepository,
			withdrawalCheckReadRepository
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
		updateMyProfileUseCase = new UpdateMyProfileService(authUserRepository, userRepository);
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
			return Optional.ofNullable(authUsers.get(userId))
				.filter(user -> !user.isWithdrawn());
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return authUsers.values().stream()
				.filter(user -> !user.isWithdrawn())
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
				.filter(user -> !user.requiresCompletion())
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

	protected static final class InMemoryWithdrawalCheckReadRepository
		implements com.bangpot.user.application.port.WithdrawalCheckReadRepository {
		private final Map<Long, View> resultsByUserId = new HashMap<>();

		@Override
		public View load(Long userId) {
			return resultsByUserId.getOrDefault(userId, View.of(List.of(), List.of()));
		}

		void putResult(Long userId, View view) {
			resultsByUserId.put(userId, view);
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
