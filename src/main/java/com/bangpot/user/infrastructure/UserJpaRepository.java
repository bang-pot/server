package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.user.domain.view.UserSearchView;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findByIdAndWithdrawnAtIsNull(Long id);

	boolean existsByIdAndWithdrawnAtIsNull(Long id);

	boolean existsByNicknameAndWithdrawnAtIsNull(String nickname);

	List<UserJpaEntity> findAllByWithdrawnAtIsNullOrderByIdAsc();

	List<UserJpaEntity> findAllByNicknameContainingIgnoreCaseAndWithdrawnAtIsNullOrderByIdAsc(String nickname);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update UserJpaEntity u
		set u.nickname = :nickname
		where u.id = :userId
		  and u.withdrawnAt is null
		""")
	int updateNicknameById(
		@Param("userId") Long userId,
		@Param("nickname") String nickname
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update UserJpaEntity u
		set u.nickname = :nickname,
		    u.profileImageUrl = :profileImageUrl
		where u.id = :userId
		  and u.withdrawnAt is null
		""")
	int updateProfileById(
		@Param("userId") Long userId,
		@Param("nickname") String nickname,
		@Param("profileImageUrl") String profileImageUrl
	);

	@Query(
		value = """
			select new com.bangpot.user.domain.view.UserSearchView$Item(
				u.id,
				u.nickname,
				u.profileImageUrl,
				u.bio,
				u.gender,
				0
			)
			from UserJpaEntity u
			where u.withdrawnAt is null
			  and lower(u.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
			order by lower(u.nickname) asc, u.id asc
			""",
		countQuery = """
			select count(u)
			from UserJpaEntity u
			where u.withdrawnAt is null
			  and lower(u.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
			"""
	)
	Page<UserSearchView.Item> searchRowsByNickname(
		@Param("keyword") String keyword,
		Pageable pageable
	);
}
