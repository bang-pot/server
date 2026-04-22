package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.domain.view.UserSearchView;

@DataJpaTest
class JpaUserRepositorySearchTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserQueryRepository repository;

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

		UserSearchView result = repository.searchByNickname("pot", 0, 20);

		assertThat(result.items()).hasSize(4);
		assertThat(result.items()).extracting(UserSearchView.Item::userId)
			.containsExactly(10L, 11L, 12L, 13L);
		assertThat(result.items()).extracting(UserSearchView.Item::nickname)
			.containsExactly("BangPot", "bangpot", "potter", "temp-pot");
		assertThat(result.items()).extracting(UserSearchView.Item::profileImageUrl)
			.containsExactly("https://cdn.example.com/users/10.jpg", null, null, null);
		assertThat(result.items()).extracting(UserSearchView.Item::bio)
			.containsExactly("bio-10", null, "bio-12", null);
		assertThat(result.items()).extracting(UserSearchView.Item::gender)
			.containsExactly("MALE", null, "FEMALE", null);
		assertThat(result.items()).extracting(UserSearchView.Item::escapeCount)
			.containsExactly(0, 0, 0, 0);
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 20, 4L, 1));
	}

	@Test
	void appliesRequestedSizeLimit() {
		insertUser(20L, "AlphaPot", null, null, null, null);
		insertUser(21L, "beta-pot", null, null, null, null);
		insertUser(22L, "pot-zone", null, null, null, null);
		entityManager.clear();

		UserSearchView result = repository.searchByNickname("pot", 0, 2);

		assertThat(result.items()).hasSize(2);
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 2, 3L, 2));
	}

	@Test
	void treatsLikeWildcardCharactersAsPlainText() {
		insertUser(30L, "under_score", null, null, null, null);
		insertUser(31L, "100%real", null, null, null, null);
		insertUser(32L, "ordinary", null, null, null, null);
		entityManager.clear();

		UserSearchView underscoreResult = repository.searchByNickname("\\_", 0, 20);
		UserSearchView percentResult = repository.searchByNickname("\\%", 0, 20);

		assertThat(underscoreResult.items()).extracting(UserSearchView.Item::userId)
			.containsExactly(30L);
		assertThat(percentResult.items()).extracting(UserSearchView.Item::userId)
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
