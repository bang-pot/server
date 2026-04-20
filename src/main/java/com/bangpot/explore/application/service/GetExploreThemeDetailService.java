package com.bangpot.explore.application.service;

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
		boolean isFavorite = query.userId() != null && themeFavoriteRepository.exists(query.userId(), query.themeId());

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
					relatedTheme.runningTimeMinutes()
				))
				.toList()
		);
	}
}
