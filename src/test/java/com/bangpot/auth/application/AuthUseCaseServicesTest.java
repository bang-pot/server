package com.bangpot.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.service.CompleteTempUserService;
import com.bangpot.auth.application.service.GetCurrentAuthUserService;
import com.bangpot.auth.application.service.LoginWithProviderService;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.application.usecase.LoginWithProviderUseCase;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;
import com.bangpot.auth.application.config.AuthRequiredTermsProperties;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.port.ProfileHubReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CheckNicknameAvailabilityService;
import com.bangpot.user.application.service.GetMyProfileService;
import com.bangpot.user.application.service.UpdateMyProfileService;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.User;

class AuthUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-03-31T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private ProfileHubReadRepository profileHubReadRepository;
	private LoginWithProviderUseCase loginWithProviderUseCase;
	private CompleteTempUserUseCase completeTempUserUseCase;
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;
	private GetMyProfileUseCase getMyProfileUseCase;
	private UpdateMyProfileUseCase updateMyProfileUseCase;
	private AuthAuditLogger authAuditLogger;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		profileHubReadRepository = userId -> ProfileHubReadRepository.Counts.of(0L, 0L, 0L, 0L);
		Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		AuthRequiredTermsProperties authRequiredTermsProperties = new AuthRequiredTermsProperties();
		authRequiredTermsProperties.setRequiredTermsVersion("2026-03-25");

		loginWithProviderUseCase = new LoginWithProviderService(authUserRepository, authAuditLogger);
		completeTempUserUseCase = new CompleteTempUserService(
			authUserRepository,
			userRepository,
			clock,
			authRequiredTermsProperties,
			authAuditLogger
		);
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		getCurrentAuthUserUseCase = new GetCurrentAuthUserService(
			authUserRepository,
			userRepository,
			authRequiredTermsProperties
		);
		getMyProfileUseCase = new GetMyProfileService(authUserRepository, userRepository, profileHubReadRepository);
		updateMyProfileUseCase = new UpdateMyProfileService(authUserRepository, userRepository);
	}

	@Test
	void logsInExistingFullUserByProviderId() {
		AuthUser existingUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"1001",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(3600)),
			null,
			NOW.minusSeconds(7200),
			NOW.minusSeconds(3600)
		);
		authUserRepository.save(existingUser);
		userRepository.save(User.rehydrate(existingUser.getId(), "bangpot"));

		LoginWithProviderUseCase.Result result = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "1001", "/protected-demo")
		);

		assertThat(result.userId()).isEqualTo(existingUser.getId());
		assertThat(result.authStatus()).isEqualTo(AuthUserStatus.FULL);
		assertThat(result.completionRequired()).isFalse();
		assertThat(result.nextPath()).isEqualTo("/protected-demo");
		verify(authAuditLogger).loginSucceeded(
			AuthProvider.KAKAO,
			existingUser.getId(),
			AuthUserStatus.FULL,
			false
		);
	}

	@Test
	void createsTempUserForNewProviderAndKeepsCompletionRequiredOnRelogin() {
		LoginWithProviderUseCase.Result firstLogin = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", "/protected-demo")
		);
		LoginWithProviderUseCase.Result secondLogin = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", null)
		);

		assertThat(firstLogin.authStatus()).isEqualTo(AuthUserStatus.TEMP);
		assertThat(firstLogin.completionRequired()).isTrue();
		assertThat(firstLogin.nextPath()).isEqualTo("/auth/complete");
		assertThat(firstLogin.pendingRedirectPath()).isEqualTo("/protected-demo");

		assertThat(secondLogin.userId()).isEqualTo(firstLogin.userId());
		assertThat(secondLogin.authStatus()).isEqualTo(AuthUserStatus.TEMP);
		assertThat(secondLogin.completionRequired()).isTrue();
		assertThat(secondLogin.nextPath()).isEqualTo("/auth/complete");
		assertThat(secondLogin.pendingRedirectPath()).isEqualTo("/protected-demo");
		verify(authAuditLogger, times(2)).loginSucceeded(
			AuthProvider.KAKAO,
			firstLogin.userId(),
			AuthUserStatus.TEMP,
			true
		);
	}

	@Test
	void returnsPendingRedirectPathOnlyForTempUser() {
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2500", "/protected-demo")
		);

		GetCurrentAuthUserUseCase.View meView = getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(loginResult.userId())
		);

		assertThat(meView.authStatus()).isEqualTo(GetCurrentAuthUserUseCase.AuthStatus.TEMP);
		assertThat(meView.redirectTo()).isEqualTo("/protected-demo");
	}

	@Test
	void completesTempUserProfileAndPromotesUserToFull() {
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "3003", "/protected-demo")
		);

		CompleteTempUserUseCase.Result completionResult = completeTempUserUseCase.handle(
			CompleteTempUserUseCase.Command.of(loginResult.userId(), "potmaster", true)
		);

		assertThat(completionResult.authStatus()).isEqualTo(AuthUserStatus.FULL);
		assertThat(completionResult.completionRequired()).isFalse();
		assertThat(completionResult.nextPath()).isEqualTo("/protected-demo");
		assertThat(userRepository.findById(loginResult.userId())).isPresent()
			.get()
			.extracting(User::getNickname)
			.isEqualTo("potmaster");

		GetCurrentAuthUserUseCase.View meView = getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(loginResult.userId())
		);
		assertThat(meView.authStatus()).isEqualTo(GetCurrentAuthUserUseCase.AuthStatus.FULL);
		assertThat(meView.user()).isNotNull();
		assertThat(meView.user().nickname()).isEqualTo("potmaster");
		assertThat(meView.redirectTo()).isNull();
		assertThat(meView.requiredTermsVersion()).isEqualTo("2026-03-25");
		verify(authAuditLogger).authStateChanged(
			loginResult.userId(),
			AuthUserStatus.TEMP,
			AuthUserStatus.FULL,
			"profile_completed"
		);
	}

	@Test
	void throwsInternalInvariantErrorWhenFullAuthUserHasNoProfile() {
		AuthUser fullUser = AuthUser.rehydrate(
			88L,
			AuthProvider.KAKAO,
			"full-without-profile",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
		authUserRepository.save(fullUser);

		assertThatThrownBy(() -> getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(fullUser.getId())
		))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("완료된 회원의 프로필 정보가 없습니다.")
			.hasMessageContaining("userId=88");
	}

	@Test
	void authUserDoesNotOwnNicknameState() {
		assertThat(AuthUser.class.getDeclaredFields())
			.extracting(java.lang.reflect.Field::getName)
			.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
			.doesNotContain("nickname");
	}

	@Test
	void rejectsDuplicateNicknameAtCompletionTime() {
		authUserRepository.save(AuthUser.rehydrate(
			10L,
			AuthProvider.KAKAO,
			"existing",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		));
		userRepository.save(User.rehydrate(10L, "dupe-name"));
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "4004", null)
		);

		assertThat(checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("dupe-name")
		).available()).isFalse();
		assertThatThrownBy(() -> completeTempUserUseCase.handle(
			CompleteTempUserUseCase.Command.of(loginResult.userId(), "dupe-name", true)
		))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	@Test
	void returnsCurrentProfileForFullUser() {
		AuthUser fullUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"full-user",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.rehydrate(fullUser.getId(), "bangpot"));

		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(fullUser.getId()));

		assertThat(result.id()).isEqualTo(fullUser.getId());
		assertThat(result.nickname()).isEqualTo("bangpot");
	}

	@Test
	void updatesNicknameForCurrentFullUser() {
		AuthUser fullUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"full-user",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.rehydrate(fullUser.getId(), "before"));

		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			UpdateMyProfileUseCase.Command.of(fullUser.getId(), "  after  ")
		);

		assertThat(result.id()).isEqualTo(fullUser.getId());
		assertThat(result.nickname()).isEqualTo("after");
		assertThat(userRepository.findById(fullUser.getId())).get()
			.extracting(User::getNickname)
			.isEqualTo("after");
	}

	@Test
	void rejectsDuplicateNicknameWhenUpdatingProfile() {
		authUserRepository.save(AuthUser.rehydrate(
			10L,
			AuthProvider.KAKAO,
			"existing",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		));
		userRepository.save(User.rehydrate(10L, "dupe-name"));
		AuthUser fullUser = AuthUser.rehydrate(
			11L,
			AuthProvider.KAKAO,
			"updating-user",
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.rehydrate(fullUser.getId(), "before"));

		assertThatThrownBy(() -> updateMyProfileUseCase.handle(
			UpdateMyProfileUseCase.Command.of(fullUser.getId(), "dupe-name")
		))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {

		private final Map<Long, AuthUser> usersById = new HashMap<>();
		private final Map<String, Long> idsByProviderId = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			Long userId = idsByProviderId.get(provider.name() + ":" + providerId);
			return userId == null ? Optional.empty() : findById(userId);
		}
		@Override
		public AuthUser save(AuthUser user) {
			if (user.getId() == null) {
				user.assignId(sequence++);
			}
			usersById.put(user.getId(), user);
			idsByProviderId.put(user.getProvider().name() + ":" + user.getProviderId(), user.getId());
			return user;
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {

		private final Map<Long, User> usersById = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		public boolean existsByNickname(String nickname) {
			return usersById.values().stream()
				.anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalizedKeyword = nickname == null ? null : nickname.trim().toLowerCase();
			if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
				normalizedKeyword = null;
			}
			final String keyword = normalizedKeyword;
			return usersById.values().stream()
				.filter(user -> keyword == null || (user.getNickname() != null && user.getNickname().toLowerCase().contains(keyword)))
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
				.toList();
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}
}
