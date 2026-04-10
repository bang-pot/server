package com.bangpot.auth.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

interface AuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);

	boolean existsByNickname(String nickname);

	List<AuthUser> findAllByStatusAndNicknameIsNotNullOrderByIdAsc(AuthUserStatus status);

	List<AuthUser> findAllByStatusAndNicknameIsNotNullAndNicknameContainingIgnoreCaseOrderByIdAsc(
		AuthUserStatus status,
		String nickname
	);
}
