package com.bangpot.auth.application.port;

import java.util.Optional;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;

public interface AuthUserRepository {

	Optional<AuthUser> findById(Long userId);

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);

	AuthUser save(AuthUser user);
}
