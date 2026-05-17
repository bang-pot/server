package com.banglog.explore.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.domain.Store;
import com.banglog.explore.domain.Theme;
import com.banglog.explore.domain.view.ExploreThemeDetailView;
import com.banglog.explore.domain.view.ExploreThemeSearchView;

@DataJpaTest
@Import(JpaExploreQueryRepository.class)
class JpaExploreQueryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ExploreQueryRepository repository;

	@BeforeEach
	void setUp() {
		entityManager.getEntityManager().createNativeQuery("""
				create table if not exists theme_genres (
					theme_id bigint not null references themes(id) on delete cascade,
					genre_id bigint not null references genres(id),
					created_at timestamptz not null default now(),
					primary key (theme_id, genre_id)
				)
				""")
			.executeUpdate();
	}

	@Test
	void searchesActiveThemesWithoutFilters() {
		Store hongdae = entityManager.persist(Store.create("Seoul Escape Hongdae", "Seoul", "Mapo", "Mapo address"));
		Theme deepBlue = entityManager.persist(Theme.create(hongdae.getId(), "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60));
		Theme laughTrack = entityManager.persist(Theme.create(hongdae.getId(), "Laugh Track", "COMEDY", null, 2, "LOW", "2-4 players", 50));
		entityManager.flush();
		linkGenre(deepBlue, "HORROR");
		linkGenre(deepBlue, "THRILLER");
		linkGenre(laughTrack, "COMEDY");

		ExploreThemeSearchView result = repository.search(
			ExploreQueryRepository.SearchCondition.of(null, List.of(), null, null, 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.pageInfo().hasNext()).isFalse();
		assertThat(result.pageInfo().totalElements()).isEqualTo(2L);
		assertThat(result.pageInfo().totalPages()).isEqualTo(1);

		ExploreThemeSearchView filtered = repository.search(
			ExploreQueryRepository.SearchCondition.of(null, List.of("HORROR"), null, null, 0, 20)
		);

		assertThat(filtered.items()).hasSize(1);
		assertThat(filtered.items().getFirst().themeName()).isEqualTo("Deep Blue");
		assertThat(filtered.items().getFirst().genres()).containsExactly("HORROR", "THRILLER");
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
		Theme laughTrack = entityManager.persist(Theme.create(hongdae.getId(), "Laugh Track", "COMEDY", null, 2, "LOW", "2-4 players", 50));
		Theme timeAttack = entityManager.persist(Theme.create(hongdae.getId(), "Time Attack", "THRILLER", null, 3, "MEDIUM", "3-5 players", 75));
		Theme lostHarbor = entityManager.persist(Theme.create(hongdae.getId(), "Lost Harbor", "ADVENTURE", null, 3, "MEDIUM", "2-4 players", 70));
		Theme codeRed = entityManager.persist(Theme.create(hongdae.getId(), "Code Red", "HORROR", null, 5, "HIGH", "2-4 players", 65));
		Theme inactiveTheme = entityManager.persist(
			Theme.create(hongdae.getId(), "Hidden Track", "COMEDY", null, 1, "LOW", "2-4 players", 45)
		);
		entityManager.persist(Theme.create(gangnam.getId(), "Another Store Theme", "HORROR", null, 4, "HIGH", "2-4 players", 60));
		entityManager.flush();
		linkGenre(target, "HORROR");
		linkGenre(laughTrack, "COMEDY");
		linkGenre(timeAttack, "THRILLER");
		linkGenre(lostHarbor, "ADVENTURE");
		linkGenre(codeRed, "HORROR");
		linkGenre(inactiveTheme, "COMEDY");

		entityManager.getEntityManager().createQuery("update Theme t set t.active = false where t.id = :themeId")
			.setParameter("themeId", inactiveTheme.getId())
			.executeUpdate();
		entityManager.clear();

		ExploreThemeDetailView result = repository.getThemeDetail(target.getId()).orElseThrow();

		assertThat(result.themeName()).isEqualTo("Deep Blue");
		assertThat(result.storeName()).isEqualTo("Seoul Escape Hongdae");
		assertThat(result.description()).isEqualTo("Deep sea mystery theme");
		assertThat(result.externalLink()).isEqualTo("https://example.com/deep-blue");
		assertThat(result.genres()).containsExactly("HORROR");
		assertThat(result.relatedThemes()).hasSize(4);
		assertThat(result.relatedThemes())
			.extracting(ExploreThemeDetailView.RelatedTheme::themeName)
			.doesNotContain("Deep Blue", "Another Store Theme", "Hidden Track");
	}

	private void linkGenre(Theme theme, String genre) {
		entityManager.getEntityManager().createNativeQuery("""
				insert into genres (name, created_at, updated_at)
				values (:name, now(), now())
				on conflict (name) do update set updated_at = excluded.updated_at
				""")
			.setParameter("name", genre)
			.executeUpdate();
		Number genreId = (Number)entityManager.getEntityManager().createNativeQuery("""
				select id
				from genres
				where name = :name
				""")
			.setParameter("name", genre)
			.getSingleResult();
		entityManager.getEntityManager().createNativeQuery("""
				insert into theme_genres (theme_id, genre_id, created_at)
				values (:themeId, :genreId, now())
				on conflict do nothing
				""")
			.setParameter("themeId", theme.getId())
			.setParameter("genreId", genreId.longValue())
			.executeUpdate();
	}
}
