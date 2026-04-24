package com.bangpot.explore.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.bangpot.explore.application.port.ThemeFavoriteRepository;

@DataJpaTest
@Import(JpaThemeFavoriteRepository.class)
class JpaThemeFavoriteRepositoryTest {

	@Autowired
	private ThemeFavoriteRepository themeFavoriteRepository;

	@Test
	void createsAndDeletesFavoritesByUserAndTheme() {
		boolean created = themeFavoriteRepository.create(7L, 5L, Instant.parse("2026-04-20T00:00:00Z"));
		boolean duplicateCreated = themeFavoriteRepository.create(7L, 5L, Instant.parse("2026-04-20T01:00:00Z"));

		assertThat(created).isTrue();
		assertThat(duplicateCreated).isFalse();
		assertThat(themeFavoriteRepository.findFavoritedThemeIds(7L, List.of(5L))).containsExactly(5L);

		boolean deleted = themeFavoriteRepository.delete(7L, 5L);
		boolean deletedAgain = themeFavoriteRepository.delete(7L, 5L);

		assertThat(deleted).isTrue();
		assertThat(deletedAgain).isFalse();
		assertThat(themeFavoriteRepository.findFavoritedThemeIds(7L, List.of(5L))).isEmpty();
	}

	@Test
	void findsFavoritedThemeIdsWithinGivenThemeIds() {
		themeFavoriteRepository.create(7L, 5L, Instant.parse("2026-04-20T00:00:00Z"));
		themeFavoriteRepository.create(7L, 8L, Instant.parse("2026-04-20T00:10:00Z"));
		themeFavoriteRepository.create(9L, 13L, Instant.parse("2026-04-20T00:20:00Z"));

		assertThat(themeFavoriteRepository.findFavoritedThemeIds(7L, List.of(5L, 8L, 13L, 21L)))
			.containsExactlyInAnyOrder(5L, 8L);
	}
}
