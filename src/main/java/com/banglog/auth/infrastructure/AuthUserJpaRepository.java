package com.banglog.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.domain.AuthUser;

interface AuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
