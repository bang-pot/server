package com.bangpot.explore.application.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;
import com.bangpot.explore.domain.view.ThemePreviewView;

public interface ExploreQueryRepository {

	ExploreThemeSearchView search(SearchCondition searchCondition);

	GetExploreFiltersUseCase.Result getFilters();

	Optional<ExploreThemeDetailView> getThemeDetail(Long themeId);

	Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames);

	ThemePreviewView findThemePreviewView(Long userId, int limit);

	record SearchCondition(
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		public static SearchCondition of(
			String keyword,
			List<String> genres,
			String region,
			String district,
			int page,
			int size
		) {
			return new SearchCondition(keyword, genres, region, district, page, size);
		}
	}

}
