package com.bangpot.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;

interface AuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);

	boolean existsByNickname(String nickname);
}
