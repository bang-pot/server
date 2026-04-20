package com.bangpot.explore.application.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

public interface ExploreThemeReadRepository {

	SearchResult search(Condition condition);

	GetExploreFiltersUseCase.Result getFilters();

	Optional<ThemeDetail> getThemeDetail(Long themeId);

	Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames);

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

	record RelatedThemeSummary(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		Integer favoriteCount
	) {
		public static RelatedThemeSummary of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			String genre,
			String posterImageUrl,
			Integer difficulty,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
			return new RelatedThemeSummary(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genre,
				posterImageUrl,
				difficulty,
				runningTimeMinutes,
				favoriteCount
			);
		}
	}

	record ThemeDetail(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		String description,
		String externalLink,
		List<RelatedThemeSummary> relatedThemes
	) {
		public static ThemeDetail of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			String genre,
			String posterImageUrl,
			Integer difficulty,
			Integer runningTimeMinutes,
			String description,
			String externalLink,
			List<RelatedThemeSummary> relatedThemes
		) {
			return new ThemeDetail(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genre,
				posterImageUrl,
				difficulty,
				runningTimeMinutes,
				description,
				externalLink,
				relatedThemes
			);
		}
	}
}
