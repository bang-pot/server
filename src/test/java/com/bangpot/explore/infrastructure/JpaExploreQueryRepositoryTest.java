package com.bangpot.explore.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.Store;
import com.bangpot.explore.domain.Theme;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;

@DataJpaTest
@Import(JpaExploreQueryRepository.class)
class JpaExploreQueryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ExploreQueryRepository repository;

	@Test
	void searchesActiveThemesWithoutFilters() {
		Store hongdae = entityManager.persist(Store.create("Seoul Escape Hongdae", "Seoul", "Mapo", "Mapo address"));
		entityManager.persist(Theme.create(hongdae.getId(), "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60));
		entityManager.persist(Theme.create(hongdae.getId(), "Laugh Track", "COMEDY", null, 2, "LOW", "2-4 players", 50));
		entityManager.flush();

		ExploreThemeSearchView result = repository.search(
			ExploreQueryRepository.SearchCondition.of(null, List.of(), null, null, 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsThemeDetailWithUpToFourRelatedThemesFromSameStore() {
		Store hongdae = entityManager.persist(Store.create("Seoul Escape Hongdae", "Seoul", "Mapo", "Mapo address"));
		Store gangnam = entityManager.persist(Store.create("Seoul Escape Gangnam", "Seoul", "Gangnam", "Gangnam address"));

		Theme target = entityManager.persist(
			Theme.create(
				hongdae.getId(),
				"Deep Blue",
				"HORROR",
				"https://image.example/deep-blue.jpg",
				4,
				"HIGH",
				"2-4 players",
				60,
				"Deep sea mystery theme",
				"https://example.com/deep-blue"
			)
		);
		entityManager.persist(Theme.create(hongdae.getId(), "Laugh Track", "COMEDY", null, 2, "LOW", "2-4 players", 50));
		entityManager.persist(Theme.create(hongdae.getId(), "Time Attack", "THRILLER", null, 3, "MEDIUM", "3-5 players", 75));
		entityManager.persist(Theme.create(hongdae.getId(), "Lost Harbor", "ADVENTURE", null, 3, "MEDIUM", "2-4 players", 70));
		entityManager.persist(Theme.create(hongdae.getId(), "Code Red", "HORROR", null, 5, "HIGH", "2-4 players", 65));
		Theme inactiveTheme = entityManager.persist(
			Theme.create(hongdae.getId(), "Hidden Track", "COMEDY", null, 1, "LOW", "2-4 players", 45)
		);
		entityManager.persist(Theme.create(gangnam.getId(), "Another Store Theme", "HORROR", null, 4, "HIGH", "2-4 players", 60));
		entityManager.flush();

		entityManager.getEntityManager().createQuery("update Theme t set t.active = false where t.id = :themeId")
			.setParameter("themeId", inactiveTheme.getId())
			.executeUpdate();
		entityManager.clear();

		ExploreThemeDetailView result = repository.getThemeDetail(target.getId()).orElseThrow();

		assertThat(result.themeName()).isEqualTo("Deep Blue");
		assertThat(result.storeName()).isEqualTo("Seoul Escape Hongdae");
		assertThat(result.description()).isEqualTo("Deep sea mystery theme");
		assertThat(result.externalLink()).isEqualTo("https://example.com/deep-blue");
		assertThat(result.relatedThemes()).hasSize(4);
		assertThat(result.relatedThemes())
			.extracting(ExploreThemeDetailView.RelatedTheme::themeName)
			.doesNotContain("Deep Blue", "Another Store Theme", "Hidden Track");
	}
}
