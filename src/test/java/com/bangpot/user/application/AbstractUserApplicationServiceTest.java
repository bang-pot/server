package com.bangpot.user.application;

import java.time.Instant;
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
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CheckNicknameAvailabilityService;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.application.service.GetMyProfileService;
import com.bangpot.user.application.service.UpdateMyProfileService;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.User;

abstract class AbstractUserApplicationServiceTest {

	protected static final Instant BASE_TIME = Instant.parse("2026-04-14T00:00:00Z");

	protected InMemoryAuthUserRepository authUserRepository;
	protected InMemoryUserRepository userRepository;
	protected CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	protected GetMyProfileUseCase getMyProfileUseCase;
	protected UpdateMyProfileUseCase updateMyProfileUseCase;
	protected CompletedUserAccessService completedUserAccessService;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		getMyProfileUseCase = new GetMyProfileService(authUserRepository, userRepository);
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
	}

	protected static final class InMemoryUserRepository implements UserRepository {
		private final Map<Long, User> users = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(users.get(userId));
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
	}
}
