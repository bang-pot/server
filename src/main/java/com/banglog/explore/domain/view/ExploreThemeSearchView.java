package com.banglog.explore.domain.view;

import java.util.List;

public record ExploreThemeSearchView(
	List<ExploreThemeSearchView.Item> items,
	ExploreThemeSearchView.PageInfo pageInfo
) {
	public static ExploreThemeSearchView of(List<ExploreThemeSearchView.Item> items, ExploreThemeSearchView.PageInfo pageInfo) {
		return new ExploreThemeSearchView(items, pageInfo);
	}

	public record Item(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		List<String> genres,
		String posterImageUrl,
		Integer difficulty,
		String activityLabel,
		String recommendedPlayers,
		Integer runningTimeMinutes,
		Integer favoriteCount,
		boolean isFavorite
	) {
		public static Item of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			List<String> genres,
			String posterImageUrl,
			Integer difficulty,
			String activityLabel,
			String recommendedPlayers,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
			return of(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genres,
				posterImageUrl,
				difficulty,
				activityLabel,
				recommendedPlayers,
				runningTimeMinutes,
				favoriteCount,
				false
			);
		}

		public static Item of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			List<String> genres,
			String posterImageUrl,
			Integer difficulty,
			String activityLabel,
			String recommendedPlayers,
			Integer runningTimeMinutes,
			Integer favoriteCount,
			boolean isFavorite
		) {
			return new Item(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genres,
				posterImageUrl,
				difficulty,
				activityLabel,
				recommendedPlayers,
				runningTimeMinutes,
				favoriteCount,
				isFavorite
			);
		}
	}

	public record PageInfo(
		int page,
		int size,
		boolean hasNext,
		long totalElements,
		int totalPages
	) {
		public static PageInfo of(int page, int size, boolean hasNext, long totalElements, int totalPages) {
			return new PageInfo(page, size, hasNext, totalElements, totalPages);
		}
	}
}
