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
import com.bangpot.user.application.port.MyFavoriteThemeReadRepository;

@DataJpaTest
class JpaMyFavoriteThemeReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MyFavoriteThemeReadRepository repository;

	@Test
	void returnsOnlyCurrentUsersActiveFavoritesInLatestOrder() {
		Store store = entityManager.persistAndFlush(Store.create("Seoul Escape", "서울", "마포", "addr"));
		Store secondStore = entityManager.persistAndFlush(Store.create("Busan Escape", "부산", "해운대", "addr"));

		Theme second = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Second Theme", "감성", "https://cdn.example.com/theme-2.jpg", 2, "활동도 낮음", "2명", 70
		));
		second.increaseFavoriteCount();
		entityManager.persistAndFlush(second);
		Theme latest = entityManager.persistAndFlush(Theme.create(
			secondStore.getId(), "Latest Theme", "공포", "https://cdn.example.com/theme-6.jpg", 5, "활동도 높음", "4명", 80
		));
		latest.increaseFavoriteCount();
		entityManager.persistAndFlush(latest);
		Theme inactive = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Inactive Theme", "공포", "https://cdn.example.com/theme-x.jpg", 3, "활동도 보통", "2-4명", 60
		));
		entityManager.getEntityManager()
			.createNativeQuery("update themes set is_active = false where id = :id")
			.setParameter("id", inactive.getId())
			.executeUpdate();

		entityManager.persistAndFlush(ThemeFavorite.create(7L, second.getId(), Instant.parse("2026-04-11T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, latest.getId(), Instant.parse("2026-04-15T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, inactive.getId(), Instant.parse("2026-04-16T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(8L, latest.getId(), Instant.parse("2026-04-16T11:00:00Z")));
		entityManager.clear();

		MyFavoriteThemeReadRepository.SearchResult result = repository.search(7L, 0, 20);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::themeId)
			.containsExactly(latest.getId(), second.getId());
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::themeName)
			.containsExactly("Latest Theme", "Second Theme");
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::storeName)
			.containsExactly("Busan Escape", "Seoul Escape");
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::regionName)
			.containsExactly("부산", "서울");
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::thumbnailUrl)
			.containsExactly("https://cdn.example.com/theme-6.jpg", "https://cdn.example.com/theme-2.jpg");
		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::favoriteCount)
			.containsExactly(1, 1);
		assertThat(result.items()).allMatch(MyFavoriteThemeReadRepository.Item::isFavorite);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void sortsByFavoriteCreatedAtDescThenThemeIdDesc() {
		Store store = entityManager.persistAndFlush(Store.create("Seoul Escape", "서울", "마포", "addr"));
		Theme first = entityManager.persistAndFlush(Theme.create(
			store.getId(), "First Theme", "공포", null, 3, "활동도 보통", "2-4명", 60
		));
		Theme second = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Second Theme", "공포", null, 3, "활동도 보통", "2-4명", 60
		));

		Instant sameInstant = Instant.parse("2026-04-15T10:00:00Z");
		entityManager.persistAndFlush(ThemeFavorite.create(7L, first.getId(), sameInstant));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, second.getId(), sameInstant));
		entityManager.clear();

		MyFavoriteThemeReadRepository.SearchResult result = repository.search(7L, 0, 20);

		assertThat(result.items()).extracting(MyFavoriteThemeReadRepository.Item::themeId)
			.containsExactly(second.getId(), first.getId());
	}

	@Test
	void returnsHasNextWhenMoreFavoritesExistThanRequestedSize() {
		Store store = entityManager.persistAndFlush(Store.create("Seoul Escape", "서울", "마포", "addr"));
		Theme first = entityManager.persistAndFlush(Theme.create(
			store.getId(), "First Theme", "공포", null, 3, "활동도 보통", "2-4명", 60
		));
		Theme second = entityManager.persistAndFlush(Theme.create(
			store.getId(), "Second Theme", "공포", null, 3, "활동도 보통", "2-4명", 60
		));

		entityManager.persistAndFlush(ThemeFavorite.create(7L, first.getId(), Instant.parse("2026-04-14T10:00:00Z")));
		entityManager.persistAndFlush(ThemeFavorite.create(7L, second.getId(), Instant.parse("2026-04-15T10:00:00Z")));
		entityManager.clear();

		MyFavoriteThemeReadRepository.SearchResult result = repository.search(7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
		assertThat(result.items().get(0).themeId()).isEqualTo(second.getId());
	}
}
