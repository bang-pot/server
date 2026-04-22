package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findByIdAndWithdrawnAtIsNull(Long id);

	boolean existsByIdAndWithdrawnAtIsNull(Long id);

	boolean existsByNicknameAndWithdrawnAtIsNull(String nickname);

	List<UserJpaEntity> findAllByWithdrawnAtIsNullOrderByIdAsc();

	List<UserJpaEntity> findAllByNicknameContainingIgnoreCaseAndWithdrawnAtIsNullOrderByIdAsc(String nickname);

	@Query("""
		select u
		from UserJpaEntity u
		where u.withdrawnAt is null
		  and lower(u.nickname) like lower(concat('%', :keyword, '%'))
		  escape '\\'
		order by lower(u.nickname) asc, u.id asc
		""")
	Page<UserJpaEntity> searchByNickname(
		@Param("keyword") String keyword,
		Pageable pageable
	);
}
