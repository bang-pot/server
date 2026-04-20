package com.bangpot.explore.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreThemesService implements GetExploreThemesUseCase {

	private final ExploreThemeReadRepository exploreThemeReadRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		ExploreThemeReadRepository.SearchResult result = exploreThemeReadRepository.search(
			ExploreThemeReadRepository.Condition.of(
				query.keyword(),
				query.genres(),
				query.region(),
				query.district(),
				query.page(),
				query.size()
			)
		);

		if (query.userId() == null) {
			return Result.of(result.items(), result.pageInfo());
		}

		List<Long> themeIds = result.items().stream()
			.map(Item::themeId)
			.toList();
		Set<Long> favoritedThemeIds = themeFavoriteRepository.findFavoritedThemeIds(query.userId(), themeIds);
		List<Item> items = result.items().stream()
			.map(item -> Item.of(
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
				favoritedThemeIds.contains(item.themeId())
			))
			.toList();

		return Result.of(items, result.pageInfo());
	}
}
