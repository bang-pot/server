package com.bangpot.explore.domain.view;

import java.util.List;

public record MyFavoriteThemesView(
	List<MyFavoriteThemesView.Item> items,
	MyFavoriteThemesView.Page page
) {
	public static MyFavoriteThemesView of(List<MyFavoriteThemesView.Item> items, MyFavoriteThemesView.Page page) {
		return new MyFavoriteThemesView(items, page);
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

	public record Page(
		int page,
		int size,
		boolean hasNext
	) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
