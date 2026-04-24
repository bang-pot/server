package com.bangpot.explore.application.usecase;

import java.util.List;

public interface GetExploreThemeDetailUseCase {

	Result handle(Query query);

	record Query(Long userId, Long themeId) {
		public static Query of(Long userId, Long themeId) {
			return new Query(userId, themeId);
		}

		public static Query of(Long themeId) {
			return of(null, themeId);
		}
	}

	record RelatedTheme(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		Integer favoriteCount,
		boolean isFavorite
	) {
		public static RelatedTheme of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			String genre,
			String posterImageUrl,
			Integer difficulty,
			Integer runningTimeMinutes,
			Integer favoriteCount,
			boolean isFavorite
		) {
			return new RelatedTheme(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genre,
				posterImageUrl,
				difficulty,
				runningTimeMinutes,
				favoriteCount,
				isFavorite
			);
		}
	}

	record Result(
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
		boolean isFavorite,
		List<RelatedTheme> relatedThemes
	) {
		public static Result of(
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
			boolean isFavorite,
			List<RelatedTheme> relatedThemes
		) {
			return new Result(
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
				isFavorite,
				relatedThemes
			);
		}
	}
}
