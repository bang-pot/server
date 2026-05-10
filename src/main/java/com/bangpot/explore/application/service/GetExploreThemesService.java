package com.bangpot.explore.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreThemesService implements GetExploreThemesUseCase {

	private final ExploreQueryRepository exploreQueryRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	@Transactional(readOnly = true)
	public ExploreThemeSearchView handle(Query query) {
		ExploreThemeSearchView result = exploreQueryRepository.search(
			ExploreQueryRepository.SearchCondition.of(
				query.keyword(),
				query.genres(),
				query.region(),
				query.district(),
				query.page(),
				query.size()
			)
		);

		if (query.userId() == null || result.items().isEmpty()) {
			return withFavoriteFlags(result, Set.of());
		}

		List<Long> themeIds = result.items().stream()
			.map(ExploreThemeSearchView.Item::themeId)
			.toList();
		Set<Long> favoritedThemeIds = themeFavoriteRepository.findFavoritedThemeIds(query.userId(), themeIds);

		return withFavoriteFlags(result, favoritedThemeIds);
	}

	private ExploreThemeSearchView withFavoriteFlags(
		ExploreThemeSearchView result,
		Set<Long> favoritedThemeIds
	) {
		return ExploreThemeSearchView.of(
			result.items().stream()
				.map(item -> ExploreThemeSearchView.Item.of(
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
					favoritedThemeIds.contains(item.themeId())
				))
				.toList(),
			result.pageInfo()
		);
	}
}
