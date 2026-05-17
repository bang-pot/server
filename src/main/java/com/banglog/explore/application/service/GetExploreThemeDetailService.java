package com.banglog.explore.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.explore.application.exception.ExploreThemeNotFoundException;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.application.port.ThemeFavoriteRepository;
import com.banglog.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.banglog.explore.domain.view.ExploreThemeDetailView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreThemeDetailService implements GetExploreThemeDetailUseCase {

	private final ExploreQueryRepository exploreQueryRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	@Transactional(readOnly = true)
	public ExploreThemeDetailView handle(Query query) {
		ExploreThemeDetailView detail = exploreQueryRepository.getThemeDetail(query.themeId())
			.orElseThrow(() -> new ExploreThemeNotFoundException(query.themeId()));
		Set<Long> favoritedThemeIds = loadFavoritedThemeIds(query.userId(), detail);
		boolean isFavorite = favoritedThemeIds.contains(query.themeId());

		return ExploreThemeDetailView.of(
			detail.themeId(),
			detail.themeName(),
			detail.storeId(),
			detail.storeName(),
			detail.regionLabel(),
			detail.genres(),
			detail.posterImageUrl(),
			detail.difficulty(),
			detail.runningTimeMinutes(),
			detail.description(),
			detail.externalLink(),
			isFavorite,
			toRelatedThemes(detail.relatedThemes(), favoritedThemeIds)
		);
	}

	private Set<Long> loadFavoritedThemeIds(Long userId, ExploreThemeDetailView detail) {
		if (userId == null) {
			return Set.of();
		}

		List<Long> themeIds = new ArrayList<>(detail.relatedThemes().size() + 1);
		themeIds.add(detail.themeId());
		detail.relatedThemes().forEach(relatedTheme -> themeIds.add(relatedTheme.themeId()));
		return themeFavoriteRepository.findFavoritedThemeIds(userId, themeIds);
	}

	private List<ExploreThemeDetailView.RelatedTheme> toRelatedThemes(
		List<ExploreThemeDetailView.RelatedTheme> relatedThemes,
		Set<Long> favoritedThemeIds
	) {
		return relatedThemes.stream()
			.map(relatedTheme -> ExploreThemeDetailView.RelatedTheme.of(
				relatedTheme.themeId(),
				relatedTheme.themeName(),
				relatedTheme.storeId(),
				relatedTheme.storeName(),
				relatedTheme.regionLabel(),
				relatedTheme.genres(),
				relatedTheme.posterImageUrl(),
				relatedTheme.difficulty(),
				relatedTheme.runningTimeMinutes(),
				relatedTheme.favoriteCount(),
				favoritedThemeIds.contains(relatedTheme.themeId())
			))
			.toList();
	}
}
