package com.bangpot.explore.presentation;

import java.util.List;

final class ExploreDto {

	private ExploreDto() {
	}

	record ExploreThemeCardResponse(
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
	}

	record PageInfoResponse(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record ExploreThemeListResponse(
		List<ExploreThemeCardResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record ExploreThemeDetailRelatedThemeResponse(
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
	}

	record ExploreThemeDetailResponse(
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
		List<ExploreThemeDetailRelatedThemeResponse> relatedThemes
	) {
	}

	record RegionFilterResponse(
		String name,
		List<String> districts
	) {
	}

	record ExploreFiltersResponse(
		List<String> genres,
		List<RegionFilterResponse> regions
	) {
	}
}
