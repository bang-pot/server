package com.bangpot.explore.application.usecase;

import java.util.List;

public interface GetExploreThemesUseCase {

	Result handle(Query query);

	record Query(
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		public static Query of(
			String keyword,
			List<String> genres,
			String region,
			String district,
			int page,
			int size
		) {
			return new Query(keyword, genres, region, district, page, size);
		}
	}

	record Item(
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
		Integer favoriteCount,
		boolean isFavorited
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
			Integer favoriteCount,
			boolean isFavorited
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
				favoriteCount,
				isFavorited
			);
		}
	}

	record PageInfo(
		int page,
		int size,
		boolean hasNext
	) {
		public static PageInfo of(int page, int size, boolean hasNext) {
			return new PageInfo(page, size, hasNext);
		}
	}

	record Result(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static Result of(List<Item> items, PageInfo pageInfo) {
			return new Result(items, pageInfo);
		}
	}
}
