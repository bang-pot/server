package com.bangpot.home.domain.view;

import java.util.List;

public record HomeThemePreviewView(
	List<HomeThemePreviewView.Item> items
) {
	public static HomeThemePreviewView of(List<HomeThemePreviewView.Item> items) {
		return new HomeThemePreviewView(items);
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
