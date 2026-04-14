package com.bangpot.explore.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.domain.Store;
import com.bangpot.explore.domain.Theme;

@DataJpaTest
@Import(JpaExploreThemeReadRepository.class)
class JpaExploreThemeReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ExploreThemeReadRepository repository;

	@Test
	void searchesActiveThemesWithoutFilters() {
		Store hongdae = entityManager.persist(Store.create("Seoul Escape Hongdae", "서울", "마포구", "서울시 마포구"));
		entityManager.persist(Theme.create(hongdae.getId(), "Deep Blue", "HORROR", null, 4, "HIGH", "2-4인", 60));
		entityManager.persist(Theme.create(hongdae.getId(), "Laugh Track", "COMEDY", null, 2, "LOW", "2-4인", 50));
		entityManager.flush();

		ExploreThemeReadRepository.SearchResult result = repository.search(
			ExploreThemeReadRepository.Condition.of(null, List.of(), null, null, 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}
}
