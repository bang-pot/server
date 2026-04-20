package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyFavoriteThemesSummaryUseCase {

	Result handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

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

	record Result(
		List<Item> items,
		Long totalCount,
		boolean hasMore
	) {
		public static Result of(List<Item> items, Long totalCount, boolean hasMore) {
			return new Result(items, totalCount, hasMore);
		}
	}
}
