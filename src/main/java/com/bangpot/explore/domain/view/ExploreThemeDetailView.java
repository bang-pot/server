package com.bangpot.explore.domain.view;

import java.util.List;

public record ExploreThemeDetailView(
	Long themeId,
	String themeName,
	Long storeId,
	String storeName,
	String regionLabel,
	List<String> genres,
	String posterImageUrl,
	Integer difficulty,
	Integer runningTimeMinutes,
	String description,
	String externalLink,
	boolean isFavorite,
	List<ExploreThemeDetailView.RelatedTheme> relatedThemes
) {
	public static ExploreThemeDetailView of(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		List<String> genres,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		String description,
		String externalLink,
		List<ExploreThemeDetailView.RelatedTheme> relatedThemes
	) {
		return of(
			themeId,
			themeName,
			storeId,
			storeName,
			regionLabel,
			genres,
			posterImageUrl,
			difficulty,
			runningTimeMinutes,
			description,
			externalLink,
			false,
			relatedThemes
		);
	}

	public static ExploreThemeDetailView of(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		List<String> genres,
		String posterImageUrl,
		Integer difficulty,
		Integer runningTimeMinutes,
		String description,
		String externalLink,
		boolean isFavorite,
		List<ExploreThemeDetailView.RelatedTheme> relatedThemes
	) {
		return new ExploreThemeDetailView(
			themeId,
			themeName,
			storeId,
			storeName,
			regionLabel,
			genres,
			posterImageUrl,
			difficulty,
			runningTimeMinutes,
			description,
			externalLink,
			isFavorite,
			relatedThemes
		);
	}

	public record RelatedTheme(
		Long themeId,
		String themeName,
		Long storeId,
		String storeName,
		String regionLabel,
		List<String> genres,
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
			List<String> genres,
			String posterImageUrl,
			Integer difficulty,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
			return of(
				themeId,
				themeName,
				storeId,
				storeName,
				regionLabel,
				genres,
				posterImageUrl,
				difficulty,
				runningTimeMinutes,
				favoriteCount,
				false
			);
		}

		public static RelatedTheme of(
			Long themeId,
			String themeName,
			Long storeId,
			String storeName,
			String regionLabel,
			List<String> genres,
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
				genres,
				posterImageUrl,
				difficulty,
				runningTimeMinutes,
				favoriteCount,
				isFavorite
			);
		}
	}
}
