package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findByIdAndWithdrawnAtIsNull(Long id);

	boolean existsByNicknameAndWithdrawnAtIsNull(String nickname);

	List<UserJpaEntity> findAllByWithdrawnAtIsNullOrderByIdAsc();

	List<UserJpaEntity> findAllByNicknameContainingIgnoreCaseAndWithdrawnAtIsNullOrderByIdAsc(String nickname);
}
