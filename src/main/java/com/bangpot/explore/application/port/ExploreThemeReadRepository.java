package com.bangpot.explore.application.port;

import java.util.List;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

public interface ExploreThemeReadRepository {

	SearchResult search(Condition condition);

	GetExploreFiltersUseCase.Result getFilters();

	record Condition(
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		public static Condition of(
			String keyword,
			List<String> genres,
			String region,
			String district,
			int page,
			int size
		) {
			return new Condition(keyword, genres, region, district, page, size);
		}
	}

	record SearchResult(
		List<GetExploreThemesUseCase.Item> items,
		GetExploreThemesUseCase.PageInfo pageInfo
	) {
		public static SearchResult of(
			List<GetExploreThemesUseCase.Item> items,
			GetExploreThemesUseCase.PageInfo pageInfo
		) {
			return new SearchResult(items, pageInfo);
		}
	}
}
