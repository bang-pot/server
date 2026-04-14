package com.bangpot.explore.presentation;

import java.util.List;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

final class ExploreDtoMapper {

	private ExploreDtoMapper() {
	}

	static GetExploreThemesUseCase.Query toQuery(
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		return GetExploreThemesUseCase.Query.of(keyword, genres, region, district, page, size);
	}

	static ExploreDto.ExploreThemeListResponse toResponse(GetExploreThemesUseCase.Result result) {
		return new ExploreDto.ExploreThemeListResponse(
			result.items().stream()
				.map(ExploreDtoMapper::toResponse)
				.toList(),
			new ExploreDto.PageInfoResponse(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static ExploreDto.ExploreFiltersResponse toResponse(GetExploreFiltersUseCase.Result result) {
		return new ExploreDto.ExploreFiltersResponse(
			result.genres(),
			result.regions().stream()
				.map(region -> new ExploreDto.RegionFilterResponse(region.name(), region.districts()))
				.toList()
		);
	}

	private static ExploreDto.ExploreThemeCardResponse toResponse(GetExploreThemesUseCase.Item item) {
		return new ExploreDto.ExploreThemeCardResponse(
			item.themeId(),
			item.themeName(),
			item.storeId(),
			item.storeName(),
			item.regionLabel(),
			item.genre(),
			item.posterImageUrl(),
			item.difficulty(),
			item.activityLabel(),
			item.recommendedPlayers(),
			item.runningTimeMinutes(),
			item.favoriteCount(),
			item.isFavorited()
		);
	}
}
