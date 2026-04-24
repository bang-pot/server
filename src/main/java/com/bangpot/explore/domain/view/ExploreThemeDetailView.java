package com.bangpot.explore.domain.view;

import java.util.List;

public record ExploreThemeDetailView(
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
	List<ExploreThemeDetailView.RelatedTheme> relatedThemes
) {
	public static ExploreThemeDetailView of(
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
		List<ExploreThemeDetailView.RelatedTheme> relatedThemes
	) {
		return new ExploreThemeDetailView(
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

	public record RelatedTheme(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		Integer favoriteCount
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
			Integer favoriteCount
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
				favoriteCount
			);
		}
	}
}
