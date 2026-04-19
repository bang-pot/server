package com.bangpot.auth.infrastructure;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

@Repository
@RequiredArgsConstructor
public class JpaAuthUserRepository implements AuthUserRepository {

	private final AuthUserJpaRepository authUserJpaRepository;

	@Override
	public Optional<AuthUser> findById(Long userId) {
		return authUserJpaRepository.findByIdAndStatusNot(userId, AuthUserStatus.WITHDRAWN);
	}

	@Override
	public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
		return authUserJpaRepository.findByProviderAndProviderIdAndStatusNot(
			provider,
			providerId,
			AuthUserStatus.WITHDRAWN
		);
	}

	@Override
	public AuthUser save(AuthUser user) {
		return authUserJpaRepository.save(user);
	}

	@Override
	public void deleteById(Long userId) {
		authUserJpaRepository.deleteById(userId);
	}
}
