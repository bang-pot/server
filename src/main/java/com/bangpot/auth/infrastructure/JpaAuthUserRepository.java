package com.bangpot.auth.infrastructure;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;

@Repository
@RequiredArgsConstructor
public class JpaAuthUserRepository implements AuthUserRepository {

	private final AuthUserJpaRepository authUserJpaRepository;

	@Override
	public Optional<AuthUser> findById(Long userId) {
		return authUserJpaRepository.findById(userId);
	}

	@Override
	public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
		return authUserJpaRepository.findByProviderAndProviderId(provider, providerId);
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
