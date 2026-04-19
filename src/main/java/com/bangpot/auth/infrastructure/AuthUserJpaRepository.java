package com.bangpot.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

interface AuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByIdAndStatusNot(Long id, AuthUserStatus status);

	Optional<AuthUser> findByProviderAndProviderIdAndStatusNot(
		AuthProvider provider,
		String providerId,
		AuthUserStatus status
	);
}
