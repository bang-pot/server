package com.bangpot.explore.domain.view;

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
		String genre,
		String posterImageUrl,
		Integer difficulty,
		String activityLabel,
		String recommendedPlayers,
		Integer runningTimeMinutes,
		Integer favoriteCount
	) {
		public static Item of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			String genre,
			String posterImageUrl,
			Integer difficulty,
			String activityLabel,
			String recommendedPlayers,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
			return new Item(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genre,
				posterImageUrl,
				difficulty,
				activityLabel,
				recommendedPlayers,
				runningTimeMinutes,
				favoriteCount
			);
		}
	}

	public record PageInfo(
		int page,
		int size,
		boolean hasNext
	) {
		public static PageInfo of(int page, int size, boolean hasNext) {
			return new PageInfo(page, size, hasNext);
		}
	}
}
