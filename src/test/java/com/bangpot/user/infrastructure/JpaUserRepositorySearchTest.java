package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.UserSearchResult;

@DataJpaTest
class JpaUserRepositorySearchTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository repository;

	@Test
	void returnsOnlyActiveUsersMatchingNicknameInStableOrder() {
		insertUser(10L, "BangPot", "bio-10", "MALE", "https://cdn.example.com/users/10.jpg", null);
		insertUser(11L, "bangpot", null, null, null, null);
		insertUser(12L, "potter", "bio-12", "FEMALE", null, null);
		insertUser(13L, "temp-pot", null, null, null, null);

		Instant withdrawnAt = Instant.parse("2026-04-20T00:00:00Z");
		insertUser(14L, "withdrawn-pot", "bio-14", "FEMALE", null, withdrawnAt);

		insertUser(16L, "other-user", null, null, null, null);
		entityManager.clear();

		var result = repository.searchByNickname("pot", 0, 20);
		var totalCount = repository.countByNickname("pot");

		assertThat(result).hasSize(4);
		assertThat(result).extracting(UserSearchResult::userId)
			.containsExactly(10L, 11L, 12L, 13L);
		assertThat(result).extracting(UserSearchResult::nickname)
			.containsExactly("BangPot", "bangpot", "potter", "temp-pot");
		assertThat(result).extracting(UserSearchResult::profileImageUrl)
			.containsExactly("https://cdn.example.com/users/10.jpg", null, null, null);
		assertThat(result).extracting(UserSearchResult::bio)
			.containsExactly("bio-10", null, "bio-12", null);
		assertThat(result).extracting(UserSearchResult::gender)
			.containsExactly("MALE", null, "FEMALE", null);
		assertThat(result).extracting(UserSearchResult::escapeCount)
			.containsExactly(0, 0, 0, 0);
		assertThat(totalCount).isEqualTo(4L);
	}

	@Test
	void appliesRequestedSizeLimit() {
		insertUser(20L, "AlphaPot", null, null, null, null);
		insertUser(21L, "beta-pot", null, null, null, null);
		insertUser(22L, "pot-zone", null, null, null, null);
		entityManager.clear();

		var result = repository.searchByNickname("pot", 0, 2);
		var totalCount = repository.countByNickname("pot");

		assertThat(result).hasSize(2);
		assertThat(totalCount).isEqualTo(3L);
	}

	@Test
	void treatsLikeWildcardCharactersAsPlainText() {
		insertUser(30L, "under_score", null, null, null, null);
		insertUser(31L, "100%real", null, null, null, null);
		insertUser(32L, "ordinary", null, null, null, null);
		entityManager.clear();

		var underscoreResult = repository.searchByNickname("\\_", 0, 20);
		var percentResult = repository.searchByNickname("\\%", 0, 20);

		assertThat(underscoreResult).extracting(UserSearchResult::userId)
			.containsExactly(30L);
		assertThat(percentResult).extracting(UserSearchResult::userId)
			.containsExactly(31L);
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
}
