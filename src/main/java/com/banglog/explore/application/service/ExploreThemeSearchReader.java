package com.banglog.explore.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.banglog.common.cache.CacheNames;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.domain.view.ExploreThemeSearchView;

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
