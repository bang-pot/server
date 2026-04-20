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
		boolean isFavorite
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
		Integer runningTimeMinutes,
		Integer favoriteCount,
		boolean isFavorite
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
		boolean isFavorite,
		List<ExploreThemeDetailRelatedThemeResponse> relatedThemes
	) {
	}

	record ThemeFavoriteResponse(
		Long themeId,
		boolean isFavorite,
		int favoriteCount
	) {
	}

	record ExploreMeetingCreateCrewResponse(
		Long crewId,
		String crewName
	) {
	}

	record ExploreMeetingCreateCrewsResponse(
		List<ExploreMeetingCreateCrewResponse> crews
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
