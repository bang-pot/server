package com.banglog.explore.domain.view;

import java.util.List;

public record ThemePreviewView(
	List<ThemePreviewView.Item> items
) {
	public static ThemePreviewView of(List<ThemePreviewView.Item> items) {
		return new ThemePreviewView(items);
	}

	public record Item(
		Long themeId,
		String themeName,
		String storeName,
		String regionLabel,
		String posterImageUrl,
		Integer favoriteCount,
		boolean isFavorite
	) {
		public static Item of(
			Long themeId,
			String themeName,
			String storeName,
			String regionLabel,
			String posterImageUrl,
			Integer favoriteCount,
			boolean isFavorite
		) {
			return new Item(themeId, themeName, storeName, regionLabel, posterImageUrl, favoriteCount, isFavorite);
		}
	}
}
