package com.bangpot.explore.presentation;

import java.util.List;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreMeetingCreateCrewsUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.explore.application.usecase.AddThemeFavoriteUseCase;
import com.bangpot.explore.application.usecase.RemoveThemeFavoriteUseCase;

final class ExploreDtoMapper {

	private ExploreDtoMapper() {
	}

	static GetExploreThemesUseCase.Query toQuery(
		Long userId,
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		return GetExploreThemesUseCase.Query.of(userId, keyword, genres, region, district, page, size);
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

	static ExploreDto.ExploreThemeDetailResponse toResponse(GetExploreThemeDetailUseCase.Result result) {
		return new ExploreDto.ExploreThemeDetailResponse(
			result.themeId(),
			result.themeName(),
			result.storeId(),
			result.storeName(),
			result.regionLabel(),
			result.genre(),
			result.posterImageUrl(),
			result.difficulty(),
			result.runningTimeMinutes(),
			result.description(),
			result.externalLink(),
			result.isFavorite(),
			result.relatedThemes().stream()
				.map(ExploreDtoMapper::toResponse)
				.toList()
		);
	}

	static ExploreDto.ThemeFavoriteResponse toResponse(AddThemeFavoriteUseCase.Result result) {
		return new ExploreDto.ThemeFavoriteResponse(result.themeId(), result.isFavorite(), result.favoriteCount());
	}

	static ExploreDto.ThemeFavoriteResponse toResponse(RemoveThemeFavoriteUseCase.Result result) {
		return new ExploreDto.ThemeFavoriteResponse(result.themeId(), result.isFavorite(), result.favoriteCount());
	}

	static ExploreDto.ExploreMeetingCreateCrewsResponse toResponse(GetExploreMeetingCreateCrewsUseCase.Result result) {
		return new ExploreDto.ExploreMeetingCreateCrewsResponse(
			result.crews().stream()
				.map(crew -> new ExploreDto.ExploreMeetingCreateCrewResponse(crew.crewId(), crew.crewName()))
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

	private static ExploreDto.ExploreThemeDetailRelatedThemeResponse toResponse(
		GetExploreThemeDetailUseCase.RelatedTheme item
	) {
		return new ExploreDto.ExploreThemeDetailRelatedThemeResponse(
			item.themeId(),
			item.themeName(),
			item.storeId(),
			item.storeName(),
			item.regionLabel(),
			item.genre(),
			item.posterImageUrl(),
			item.difficulty(),
			item.runningTimeMinutes()
		);
	}
}
