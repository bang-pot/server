package com.bangpot.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.auth.application.exception.DuplicateNicknameException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.service.CheckNicknameAvailabilityService;
import com.bangpot.auth.application.service.CompleteTempUserService;
import com.bangpot.auth.application.service.GetCurrentAuthUserService;
import com.bangpot.auth.application.service.LoginWithProviderService;
import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.application.usecase.LoginWithProviderUseCase;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.auth.infrastructure.config.AuthRequiredTermsProperties;

class AuthUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-03-31T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private LoginWithProviderUseCase loginWithProviderUseCase;
	private CompleteTempUserUseCase completeTempUserUseCase;
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		AuthRequiredTermsProperties authRequiredTermsProperties = new AuthRequiredTermsProperties();
		authRequiredTermsProperties.setRequiredTermsVersion("2026-03-25");

		loginWithProviderUseCase = new LoginWithProviderService(authUserRepository);
		completeTempUserUseCase = new CompleteTempUserService(
			authUserRepository,
			clock,
			authRequiredTermsProperties
		);
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(authUserRepository);
		getCurrentAuthUserUseCase = new GetCurrentAuthUserService(
			authUserRepository,
			authRequiredTermsProperties
		);
	}

	@Test
	void logsInExistingFullUserByProviderId() {
		AuthUser existingUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"1001",
			AuthUserStatus.FULL,
			"bangpot",
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(3600)),
			null,
			NOW.minusSeconds(7200),
			NOW.minusSeconds(3600)
		);
		authUserRepository.save(existingUser);

		LoginWithProviderUseCase.Result result = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "1001", "/protected-demo")
		);

		assertThat(result.userId()).isEqualTo(existingUser.getId());
		assertThat(result.authStatus()).isEqualTo(AuthUserStatus.FULL);
		assertThat(result.completionRequired()).isFalse();
		assertThat(result.nextPath()).isEqualTo("/protected-demo");
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

		GetCurrentAuthUserUseCase.View meView = getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(loginResult.userId())
		);
		assertThat(meView.authStatus()).isEqualTo(GetCurrentAuthUserUseCase.AuthStatus.FULL);
		assertThat(meView.user()).isNotNull();
		assertThat(meView.user().nickname()).isEqualTo("potmaster");
		assertThat(meView.requiredTermsVersion()).isEqualTo("2026-03-25");
	}

	@Test
	void rejectsDuplicateNicknameAtCompletionTime() {
		authUserRepository.save(AuthUser.rehydrate(
			10L,
			AuthProvider.KAKAO,
			"existing",
			AuthUserStatus.FULL,
			"dupe-name",
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		));
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
		public boolean existsByNickname(String nickname) {
			return usersById.values().stream()
				.anyMatch(user -> nickname.equals(user.getNickname()));
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
}
