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
	public Result handle(Query query) {
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
			return Result.of(toItems(result.items(), Set.of()), toPageInfo(result.pageInfo()));
		}

		List<Long> themeIds = result.items().stream()
			.map(ExploreThemeSearchView.Item::themeId)
			.toList();
		Set<Long> favoritedThemeIds = themeFavoriteRepository.findFavoritedThemeIds(query.userId(), themeIds);
		List<Item> items = toItems(result.items(), favoritedThemeIds);

		return Result.of(items, toPageInfo(result.pageInfo()));
	}

	private List<Item> toItems(List<ExploreThemeSearchView.Item> items, Set<Long> favoritedThemeIds) {
		return items.stream()
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
	}

	private PageInfo toPageInfo(ExploreThemeSearchView.PageInfo pageInfo) {
		return PageInfo.of(pageInfo.page(), pageInfo.size(), pageInfo.hasNext());
	}
}
