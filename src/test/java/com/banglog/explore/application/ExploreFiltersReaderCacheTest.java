package com.banglog.explore.application;

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

import com.banglog.common.cache.CacheNames;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.application.service.ExploreFiltersReader;
import com.banglog.explore.domain.view.ExploreFiltersView;
import com.banglog.explore.domain.view.ExploreThemeDetailView;
import com.banglog.explore.domain.view.ExploreThemeSearchView;
import com.banglog.explore.domain.view.ThemePreviewView;

@SpringJUnitConfig(classes = ExploreFiltersReaderCacheTest.TestConfig.class)
class ExploreFiltersReaderCacheTest {

	@Autowired
	private ExploreFiltersReader reader;

	@Autowired
	private CountingExploreQueryRepository repository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		repository.reset();
		cacheManager.getCache(CacheNames.EXPLORE_FILTERS).clear();
	}

	@Test
	void reusesCachedExploreFilters() {
		ExploreFiltersView first = reader.getFilters();
		ExploreFiltersView second = reader.getFilters();

		assertThat(repository.filtersCallCount).isOne();
		assertThat(second).isEqualTo(first);
	}

	@Configuration
	@EnableCaching(proxyTargetClass = true)
	static class TestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheNames.EXPLORE_FILTERS);
		}

		@Bean
		CountingExploreQueryRepository exploreQueryRepository() {
			return new CountingExploreQueryRepository();
		}

		@Bean
		ExploreFiltersReader exploreFiltersReader(ExploreQueryRepository exploreQueryRepository) {
			return new ExploreFiltersReader(exploreQueryRepository);
		}
	}

	static class CountingExploreQueryRepository implements ExploreQueryRepository {

		private int filtersCallCount;

		void reset() {
			filtersCallCount = 0;
		}

		@Override
		public ExploreThemeSearchView search(SearchCondition searchCondition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ExploreFiltersView getFilters() {
			filtersCallCount++;
			return ExploreFiltersView.of(
				List.of("HORROR", "MYSTERY"),
				List.of(
					ExploreFiltersView.Region.of("SEOUL", List.of("GANGNAM", "MAPO")),
					ExploreFiltersView.Region.of("GANGWON", List.of("WONJU", "JEONGSEON"))
				)
			);
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
