package com.bangpot.auth.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

interface AuthUserJpaRepository extends JpaRepository<AuthUser, Long> {

	Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId);

	boolean existsByNickname(String nickname);

	@Query("""
		select user
		from AuthUser user
		where user.status = :status
		  and user.nickname is not null
		  and (:nickname is null or lower(user.nickname) like lower(concat('%', :nickname, '%')))
		order by user.id asc
		""")
	List<AuthUser> findAllFullUsersByNicknameContaining(
		@Param("status") AuthUserStatus status,
		@Param("nickname") String nickname
	);
}
