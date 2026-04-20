package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.explore.domain.Store;
import com.bangpot.explore.domain.Theme;
import com.bangpot.explore.domain.ThemeFavorite;
import com.bangpot.user.application.port.FavoriteThemeSummaryReadRepository;

@DataJpaTest
class JpaFavoriteThemeSummaryReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FavoriteThemeSummaryReadRepository repository;

	@Test
	void returnsLatestFiveActiveFavoritedThemesWithTotalCount() {
		Store store = entityManager.persistAndFlush(Store.create("Seoul Escape", "서울", "마포", "addr"));
		Store secondStore = entityManager.persistAndFlush(Store.create("Busan Escape", "부산", "해운대", "addr"));

		Theme oldest = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Old Theme", "공포", "https://cdn.example.com/theme-1.jpg", 3, "활동성 보통", "2-4명", 60
		));
		oldest.increaseFavoriteCount();
		entityManager.persistAndFlush(oldest);
		Theme second = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Second Theme", "감성", "https://cdn.example.com/theme-2.jpg", 2, "활동성 낮음", "2명", 70
		));
		second.increaseFavoriteCount();
		entityManager.persistAndFlush(second);
		Theme third = entityManager.persistAndFlush(Theme.create(
			secondStore.getId(), "Third Theme", "추리", null, 4, "활동성 높음", "3-5명", 75
		));
		third.increaseFavoriteCount();
		entityManager.persistAndFlush(third);
		Theme fourth = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Fourth Theme", "스릴러", "https://cdn.example.com/theme-4.jpg", 3, "활동성 높음", "2-4명", 65
		));
		fourth.increaseFavoriteCount();
		entityManager.persistAndFlush(fourth);
		Theme fifth = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Fifth Theme", "판타지", "https://cdn.example.com/theme-5.jpg", 1, "활동성 낮음", "2-3명", 55
		));
		fifth.increaseFavoriteCount();
		entityManager.persistAndFlush(fifth);
		Theme latest = entityManager.persistAndFlush(Theme.create(
			secondStore.getId(), "Latest Theme", "공포", "https://cdn.example.com/theme-6.jpg", 5, "활동성 높음", "4명", 80
		));
		latest.increaseFavoriteCount();
		entityManager.persistAndFlush(latest);

		Theme inactive = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Inactive Theme", "공포", "https://cdn.example.com/theme-x.jpg", 3, "활동성 보통", "2-4명", 60
		));
		entityManager.getEntityManager()
			.createNativeQuery("update themes set is_active = false where id = :id")
			.setParameter("id", inactive.getId())
			.executeUpdate();

		entityManager.persistAndFlush(ThemeFavorite.create(7L, oldest.getId(), Instant.parse("2026-04-10T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, second.getId(), Instant.parse("2026-04-11T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, third.getId(), Instant.parse("2026-04-12T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, fourth.getId(), Instant.parse("2026-04-13T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, fifth.getId(), Instant.parse("2026-04-14T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, latest.getId(), Instant.parse("2026-04-15T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, inactive.getId(), Instant.parse("2026-04-16T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(8L, latest.getId(), Instant.parse("2026-04-16T11:00:00Z")));

		entityManager.clear();

		FavoriteThemeSummaryReadRepository.View result = repository.load(7L, 5);

		assertThat(result.totalCount()).isEqualTo(6L);
		assertThat(result.items()).hasSize(5);
		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::themeId)
			.containsExactly(latest.getId(), fifth.getId(), fourth.getId(), third.getId(), second.getId());
		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::themeName)
			.containsExactly("Latest Theme", "Fifth Theme", "Fourth Theme", "Third Theme", "Second Theme");
		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::regionName)
			.containsExactly("부산", "서울", "서울", "부산", "서울");
		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::thumbnailUrl)
			.containsExactly(
				"https://cdn.example.com/theme-6.jpg",
				"https://cdn.example.com/theme-5.jpg",
				"https://cdn.example.com/theme-4.jpg",
				null,
				"https://cdn.example.com/theme-2.jpg"
			);
		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::favoriteCount)
			.containsExactly(1, 1, 1, 1, 1);
		assertThat(result.items()).allMatch(FavoriteThemeSummaryReadRepository.Item::isFavorite);
	}

	@Test
	void sortsByFavoriteCreatedAtDescThenThemeIdDesc() {
		Store store = entityManager.persistAndFlush(Store.create("Seoul Escape", "서울", "마포", "addr"));
		Theme first = entityManager.persistAndFlush(Theme.create(
			store.getId(), "First Theme", "공포", null, 3, "활동성 보통", "2-4명", 60
		));
		Theme second = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Second Theme", "공포", null, 3, "활동성 보통", "2-4명", 60
		));

		Instant sameInstant = Instant.parse("2026-04-15T10:00:00Z");
		entityManager.persistAndFlush(ThemeFavorite.create(7L, first.getId(), sameInstant));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, second.getId(), sameInstant));
		entityManager.clear();

		FavoriteThemeSummaryReadRepository.View result = repository.load(7L, 5);

		assertThat(result.items()).extracting(FavoriteThemeSummaryReadRepository.Item::themeId)
			.containsExactly(second.getId(), first.getId());
	}
}
