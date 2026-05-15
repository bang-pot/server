package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.bangpot.common.cache.CacheNames;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.application.service.ExploreThemeSearchReader;
import com.bangpot.explore.application.service.ThemeSearchCachePolicy;
import com.bangpot.explore.domain.view.ExploreFiltersView;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;
import com.bangpot.explore.domain.view.ThemePreviewView;

@SpringJUnitConfig(classes = ExploreThemeSearchReaderCacheTest.TestConfig.class)
class ExploreThemeSearchReaderCacheTest {

	@Autowired
	private ExploreThemeSearchReader reader;

	@Autowired
	private CountingExploreQueryRepository repository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		repository.reset();
		cacheManager.getCache(CacheNames.EXPLORE_THEME_SEARCH).clear();
	}

	@Test
	void reusesCachedResultForCacheableThemeSearch() {
		ExploreQueryRepository.SearchCondition condition = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of("HORROR"),
			"SEOUL",
			null,
			0,
			20
		);

		reader.search(condition);
		reader.search(condition);

		assertThat(repository.searchCallCount).isOne();
	}

	@Test
	void doesNotCacheKeywordSearch() {
		ExploreQueryRepository.SearchCondition condition = ExploreQueryRepository.SearchCondition.of(
			"gangnam",
			List.of(),
			null,
			null,
			0,
			20
		);

		reader.search(condition);
		reader.search(condition);

		assertThat(repository.searchCallCount).isEqualTo(2);
	}

	@Configuration
	@EnableCaching(proxyTargetClass = true)
	static class TestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheNames.EXPLORE_THEME_SEARCH);
		}

		@Bean
		ThemeSearchCachePolicy themeSearchCachePolicy() {
			return new ThemeSearchCachePolicy();
		}

		@Bean
		CountingExploreQueryRepository exploreQueryRepository() {
			return new CountingExploreQueryRepository();
		}

		@Bean
		ExploreThemeSearchReader exploreThemeSearchReader(ExploreQueryRepository exploreQueryRepository) {
			return new ExploreThemeSearchReader(exploreQueryRepository);
		}
	}

	static class CountingExploreQueryRepository implements ExploreQueryRepository {

		private int searchCallCount;

		void reset() {
			searchCallCount = 0;
		}

		@Override
		public ExploreThemeSearchView search(SearchCondition searchCondition) {
			searchCallCount++;
			return ExploreThemeSearchView.of(
				List.of(),
				ExploreThemeSearchView.PageInfo.of(searchCondition.page(), searchCondition.size(), false, 0, 0)
			);
		}

		@Override
		public ExploreFiltersView getFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<ExploreThemeDetailView> getThemeDetail(Long themeId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ThemePreviewView findThemePreviewView(Long userId, int limit) {
			throw new UnsupportedOperationException();
		}
	}
}
