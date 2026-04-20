package com.bangpot.explore.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreThemeDetailService implements GetExploreThemeDetailUseCase {

	private final ExploreThemeReadRepository exploreThemeReadRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		ExploreThemeReadRepository.ThemeDetail detail = exploreThemeReadRepository.getThemeDetail(query.themeId())
			.orElseThrow(() -> new ExploreThemeNotFoundException(query.themeId()));
		List<Long> favoriteTargetThemeIds = query.userId() == null
			? List.of()
			: java.util.stream.Stream.concat(
				java.util.stream.Stream.of(detail.themeId()),
				detail.relatedThemes().stream().map(ExploreThemeReadRepository.RelatedThemeSummary::themeId)
			).toList();
		Set<Long> favoritedThemeIds = query.userId() == null
			? Set.of()
			: themeFavoriteRepository.findFavoritedThemeIds(query.userId(), favoriteTargetThemeIds);
		boolean isFavorite = favoritedThemeIds.contains(query.themeId());

		return Result.of(
			detail.themeId(),
			detail.themeName(),
			detail.storeId(),
			detail.storeName(),
			detail.regionLabel(),
			detail.genre(),
			detail.posterImageUrl(),
			detail.difficulty(),
			detail.runningTimeMinutes(),
			detail.description(),
			detail.externalLink(),
			isFavorite,
			detail.relatedThemes().stream()
				.map(relatedTheme -> RelatedTheme.of(
					relatedTheme.themeId(),
					relatedTheme.themeName(),
					relatedTheme.storeId(),
					relatedTheme.storeName(),
					relatedTheme.regionLabel(),
					relatedTheme.genre(),
					relatedTheme.posterImageUrl(),
					relatedTheme.difficulty(),
					relatedTheme.runningTimeMinutes(),
					relatedTheme.favoriteCount(),
					favoritedThemeIds.contains(relatedTheme.themeId())
				))
				.toList()
		);
	}
}
