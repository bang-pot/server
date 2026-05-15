package com.bangpot.explore.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.bangpot.common.cache.CacheNames;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExploreThemeSearchReader {

	private final ExploreQueryRepository exploreQueryRepository;

	@Cacheable(
		cacheNames = CacheNames.EXPLORE_THEME_SEARCH,
		condition = "@themeSearchCachePolicy.cacheable(#p0)",
		key = "@themeSearchCachePolicy.keyOf(#p0)"
	)
	public ExploreThemeSearchView search(ExploreQueryRepository.SearchCondition searchCondition) {
		return exploreQueryRepository.search(searchCondition);
	}
}
