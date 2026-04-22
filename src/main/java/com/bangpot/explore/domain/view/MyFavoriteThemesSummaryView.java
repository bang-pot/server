package com.bangpot.explore.domain.view;

import java.util.List;

public record MyFavoriteThemesSummaryView(
	List<MyFavoriteThemesSummaryView.Item> items,
	Long totalCount,
	boolean hasMore
) {
	public static MyFavoriteThemesSummaryView of(
		List<MyFavoriteThemesSummaryView.Item> items,
		Long totalCount,
		boolean hasMore
	) {
		return new MyFavoriteThemesSummaryView(items, totalCount, hasMore);
	}

	public record Item(
		Long themeId,
		String themeName,
		String storeName,
		String regionName,
		String thumbnailUrl,
		Integer favoriteCount,
		boolean isFavorite
	) {
		public static Item of(
			Long themeId,
			String themeName,
			String storeName,
			String regionName,
			String thumbnailUrl,
			Integer favoriteCount,
			boolean isFavorite
		) {
			return new Item(themeId, themeName, storeName, regionName, thumbnailUrl, favoriteCount, isFavorite);
		}
	}
}
