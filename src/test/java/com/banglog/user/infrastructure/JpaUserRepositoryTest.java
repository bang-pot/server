package com.banglog.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.user.application.port.UserRepository;

@DataJpaTest
@Import(JpaUserRepository.class)
class JpaUserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	void returnsEmptyForBlankNicknameKeyword() {
		insertUser(10L, "banglog", null);
		insertUser(11L, "potter", null);
		entityManager.clear();

		assertThat(userRepository.findCompletedUsersByNicknameContaining("   ")).isEmpty();
	}

	private void insertUser(Long userId, String nickname, Instant withdrawnAt) {
		Instant now = Instant.parse("2026-04-21T10:00:00Z").plus(userId, ChronoUnit.SECONDS);
		entityManager.getEntityManager()
			.createNativeQuery("""
				insert into users (id, nickname, bio, gender, profile_image_url, created_at, updated_at, withdrawn_at)
				values (:id, :nickname, null, null, null, :createdAt, :updatedAt, :withdrawnAt)
				""")
			.setParameter("id", userId)
			.setParameter("nickname", nickname)
			.setParameter("createdAt", now)
			.setParameter("updatedAt", now)
			.setParameter("withdrawnAt", withdrawnAt)
			.executeUpdate();
	}
}
