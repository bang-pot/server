package com.banglog.explore.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.explore.application.port.ThemeRepository;
import com.banglog.explore.domain.Theme;

@DataJpaTest
@Import(JpaThemeRepository.class)
class JpaThemeRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ThemeRepository repository;

	@Test
	void increasesFavoriteCountWithAtomicUpdate() {
		Theme theme = entityManager.persistAndFlush(
			Theme.create(1L, "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60)
		);
		entityManager.clear();

		boolean firstUpdated = repository.increaseFavoriteCount(theme.getId());
		boolean secondUpdated = repository.increaseFavoriteCount(theme.getId());

		assertThat(firstUpdated).isTrue();
		assertThat(secondUpdated).isTrue();
		assertThat(repository.findActiveFavoriteCountById(theme.getId())).contains(2);
	}

	@Test
	void decreasesFavoriteCountWithAtomicUpdate() {
		Theme theme = entityManager.persistAndFlush(
			Theme.create(1L, "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60)
		);
		repository.increaseFavoriteCount(theme.getId());
		repository.increaseFavoriteCount(theme.getId());

		boolean updated = repository.decreaseFavoriteCount(theme.getId());

		assertThat(updated).isTrue();
		assertThat(repository.findActiveFavoriteCountById(theme.getId())).contains(1);
	}

	@Test
	void returnsFalseWhenDecreasingZeroFavoriteCount() {
		Theme theme = entityManager.persistAndFlush(
			Theme.create(1L, "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60)
		);

		assertThat(repository.decreaseFavoriteCount(theme.getId())).isFalse();
		assertThat(repository.findActiveFavoriteCountById(theme.getId())).contains(0);
	}

	@Test
	void returnsFalseWhenIncreasingMissingTheme() {
		assertThat(repository.increaseFavoriteCount(999L)).isFalse();
		assertThat(repository.decreaseFavoriteCount(999L)).isFalse();
		assertThat(repository.findActiveFavoriteCountById(999L)).isEmpty();
	}
}
