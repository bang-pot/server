package com.bangpot.user.application.port;

import java.util.List;

public interface FavoriteThemeSummaryReadRepository {

	View load(Long userId, int limit);

	record Item(
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

	record View(
		List<Item> items,
		Long totalCount
	) {
		public static View of(List<Item> items, Long totalCount) {
			return new View(items, totalCount);
		}
	}
}
