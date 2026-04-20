package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.user.application.port.UserSearchReadRepository;

interface JpaUserSearchReadRepository extends Repository<UserJpaEntity, Long>, UserSearchReadRepository {

	@Override
	default List<Item> search(String keyword, int size) {
		return searchRows(keyword, AuthUserStatus.FULL, PageRequest.of(0, size)).stream()
			.map(row -> Item.of(
				row.getUserId(),
				row.getNickname(),
				row.getProfileImageUrl(),
				row.getBio(),
				row.getGender(),
				0
			))
			.toList();
	}

	@Query("""
		select u.id as userId,
		       u.nickname as nickname,
		       u.profileImageUrl as profileImageUrl,
		       u.bio as bio,
		       u.gender as gender
		from UserJpaEntity u, AuthUser a
		where u.id = a.id
		  and a.status = :status
		  and u.withdrawnAt is null
		  and lower(u.nickname) like lower(concat('%', :keyword, '%'))
		order by lower(u.nickname) asc, u.id asc
		""")
	List<Row> searchRows(
		@Param("keyword") String keyword,
		@Param("status") AuthUserStatus status,
		Pageable pageable
	);

	interface Row {
		Long getUserId();

		String getNickname();

		String getProfileImageUrl();

		String getBio();

		String getGender();
	}
}
