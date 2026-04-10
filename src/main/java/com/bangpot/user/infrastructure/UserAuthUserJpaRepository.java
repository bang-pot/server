package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

interface UserAuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	boolean existsByNickname(String nickname);

	List<AuthUser> findAllByStatusAndNicknameIsNotNullOrderByIdAsc(AuthUserStatus status);

	List<AuthUser> findAllByStatusAndNicknameIsNotNullAndNicknameContainingIgnoreCaseOrderByIdAsc(
		AuthUserStatus status,
		String nickname
	);
}
