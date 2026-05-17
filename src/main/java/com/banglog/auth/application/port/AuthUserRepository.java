package com.banglog.auth.application.port;

import java.util.Optional;

import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.domain.AuthUser;

public interface AuthUserRepository {

	Optional<AuthUser> findById(Long userId);

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);

	AuthUser save(AuthUser user);

	void deleteById(Long userId);
}
