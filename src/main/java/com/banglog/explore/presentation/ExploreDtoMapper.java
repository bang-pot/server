package com.banglog.explore.presentation;

import java.util.List;

import com.banglog.explore.application.usecase.AddThemeFavoriteUseCase;
import com.banglog.explore.application.usecase.GetExploreThemesUseCase;
import com.banglog.explore.application.usecase.RemoveThemeFavoriteUseCase;
import com.banglog.explore.domain.view.ExploreFiltersView;
import com.banglog.explore.domain.view.ExploreThemeDetailView;
import com.banglog.explore.domain.view.ExploreThemeSearchView;

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

	static ExploreDto.ExploreThemeListResponse toResponse(ExploreThemeSearchView result) {
		return new ExploreDto.ExploreThemeListResponse(
			result.items().stream()
				.map(ExploreDtoMapper::toResponse)
				.toList(),
			new ExploreDto.PageInfoResponse(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext(),
				result.pageInfo().totalElements(),
				result.pageInfo().totalPages()
			)
		);
	}

	static ExploreDto.ExploreFiltersResponse toResponse(ExploreFiltersView result) {
		return new ExploreDto.ExploreFiltersResponse(
			result.genres(),
			result.regions().stream()
				.map(region -> new ExploreDto.RegionFilterResponse(region.name(), region.districts()))
				.toList()
		);
	}

	static ExploreDto.ExploreThemeDetailResponse toResponse(ExploreThemeDetailView result) {
		return new ExploreDto.ExploreThemeDetailResponse(
			result.themeId(),
			result.themeName(),
			result.storeId(),
			result.storeName(),
			result.regionLabel(),
			result.genres(),
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

	private static ExploreDto.ExploreThemeCardResponse toResponse(ExploreThemeSearchView.Item item) {
		return new ExploreDto.ExploreThemeCardResponse(
			item.themeId(),
			item.themeName(),
			item.storeId(),
			item.storeName(),
			item.regionLabel(),
			item.genres(),
			item.posterImageUrl(),
			item.difficulty(),
			item.activityLabel(),
			item.recommendedPlayers(),
			item.runningTimeMinutes(),
			item.favoriteCount(),
			item.isFavorite()
		);
	}

	private static ExploreDto.ExploreThemeDetailRelatedThemeResponse toResponse(
		ExploreThemeDetailView.RelatedTheme item
	) {
		return new ExploreDto.ExploreThemeDetailRelatedThemeResponse(
			item.themeId(),
			item.themeName(),
			item.storeId(),
			item.storeName(),
			item.regionLabel(),
			item.genres(),
			item.posterImageUrl(),
			item.difficulty(),
			item.runningTimeMinutes(),
			item.favoriteCount(),
			item.isFavorite()
		);
	}
}
