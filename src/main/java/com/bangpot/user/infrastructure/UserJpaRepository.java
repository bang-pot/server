package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	boolean existsByNickname(String nickname);

	List<UserJpaEntity> findAllByOrderByIdAsc();

	List<UserJpaEntity> findAllByNicknameContainingIgnoreCaseOrderByIdAsc(String nickname);
}
