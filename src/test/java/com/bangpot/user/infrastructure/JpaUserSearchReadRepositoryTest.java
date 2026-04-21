package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.user.application.port.UserSearchReadRepository;

@DataJpaTest
class JpaUserSearchReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserSearchReadRepository repository;

	@Test
	void returnsOnlyFullActiveUsersMatchingNicknameInStableOrder() {
		insertUser(10L, "BangPot", "bio-10", "MALE", "https://cdn.example.com/users/10.jpg", null);
		insertAuthUser(10L, "provider-10", AuthUserStatus.FULL, null);

		insertUser(11L, "bangpot", null, null, null, null);
		insertAuthUser(11L, "provider-11", AuthUserStatus.FULL, null);

		insertUser(12L, "potter", "bio-12", "FEMALE", null, null);
		insertAuthUser(12L, "provider-12", AuthUserStatus.FULL, null);

		insertUser(13L, "temp-pot", null, null, null, null);
		insertAuthUser(13L, "provider-13", AuthUserStatus.TEMP, null);

		Instant withdrawnAt = Instant.parse("2026-04-20T00:00:00Z");
		insertUser(14L, "withdrawn-pot", "bio-14", "FEMALE", null, withdrawnAt);
		insertAuthUser(14L, "provider-14", AuthUserStatus.FULL, null);

		insertUser(15L, "gone-pot", "bio-15", "MALE", null, null);
		insertAuthUser(15L, "provider-15", AuthUserStatus.WITHDRAWN, withdrawnAt);

		insertUser(16L, "other-user", null, null, null, null);
		insertAuthUser(16L, "provider-16", AuthUserStatus.FULL, null);
		entityManager.clear();

		var result = repository.search("pot", 20);

		assertThat(result).hasSize(3);
		assertThat(result).extracting(UserSearchReadRepository.Item::userId)
			.containsExactly(10L, 11L, 12L);
		assertThat(result).extracting(UserSearchReadRepository.Item::nickname)
			.containsExactly("BangPot", "bangpot", "potter");
		assertThat(result).extracting(UserSearchReadRepository.Item::profileImageUrl)
			.containsExactly("https://cdn.example.com/users/10.jpg", null, null);
		assertThat(result).extracting(UserSearchReadRepository.Item::bio)
			.containsExactly("bio-10", null, "bio-12");
		assertThat(result).extracting(UserSearchReadRepository.Item::gender)
			.containsExactly("MALE", null, "FEMALE");
		assertThat(result).extracting(UserSearchReadRepository.Item::escapeCount)
			.containsExactly(0, 0, 0);
	}

	@Test
	void appliesRequestedSizeLimit() {
		insertUser(20L, "AlphaPot", null, null, null, null);
		insertAuthUser(20L, "provider-20", AuthUserStatus.FULL, null);
		insertUser(21L, "beta-pot", null, null, null, null);
		insertAuthUser(21L, "provider-21", AuthUserStatus.FULL, null);
		insertUser(22L, "pot-zone", null, null, null, null);
		insertAuthUser(22L, "provider-22", AuthUserStatus.FULL, null);
		entityManager.clear();

		var result = repository.search("pot", 2);

		assertThat(result).hasSize(2);
	}

	private void insertUser(
		Long userId,
		String nickname,
		String bio,
		String gender,
		String profileImageUrl,
		Instant withdrawnAt
	) {
		Instant now = Instant.parse("2026-04-20T10:00:00Z").plus(userId, ChronoUnit.SECONDS);
		entityManager.getEntityManager()
			.createNativeQuery("""
				insert into users (id, nickname, bio, gender, profile_image_url, created_at, updated_at, withdrawn_at)
				values (:id, :nickname, :bio, :gender, :profileImageUrl, :createdAt, :updatedAt, :withdrawnAt)
				""")
			.setParameter("id", userId)
			.setParameter("nickname", nickname)
			.setParameter("bio", bio)
			.setParameter("gender", gender)
			.setParameter("profileImageUrl", profileImageUrl)
			.setParameter("createdAt", now)
			.setParameter("updatedAt", now)
			.setParameter("withdrawnAt", withdrawnAt)
			.executeUpdate();
	}

	private void insertAuthUser(
		Long userId,
		String providerId,
		AuthUserStatus status,
		Instant withdrawnAt
	) {
		Instant now = Instant.parse("2026-04-20T10:00:00Z").plus(userId, ChronoUnit.SECONDS);
		entityManager.getEntityManager()
			.createNativeQuery("""
				insert into auth_users (
					id,
					provider,
					provider_id,
					status,
					required_terms_version,
					required_terms_accepted_at,
					pending_redirect_path,
					created_at,
					updated_at,
					withdrawn_at
				)
				values (
					:id,
					:provider,
					:providerId,
					:status,
					:requiredTermsVersion,
					:requiredTermsAcceptedAt,
					null,
					:createdAt,
					:updatedAt,
					:withdrawnAt
				)
				""")
			.setParameter("id", userId)
			.setParameter("provider", "KAKAO")
			.setParameter("providerId", providerId)
			.setParameter("status", status.name())
			.setParameter("requiredTermsVersion", status == AuthUserStatus.FULL ? "2026-04" : null)
			.setParameter("requiredTermsAcceptedAt", status == AuthUserStatus.FULL ? now : null)
			.setParameter("createdAt", now)
			.setParameter("updatedAt", now)
			.setParameter("withdrawnAt", withdrawnAt)
			.executeUpdate();
	}
}
