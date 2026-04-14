package com.bangpot.explore.application.usecase;

import java.util.List;

public interface GetExploreThemeDetailUseCase {

	Result handle(Query query);

	record Query(Long themeId) {
		public static Query of(Long themeId) {
			return new Query(themeId);
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
		Integer runningTimeMinutes
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
			Integer runningTimeMinutes
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
				runningTimeMinutes
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
				relatedThemes
			);
		}
	}
}
