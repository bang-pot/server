package com.banglog.explore.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.banglog.common.cache.CacheNames;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.domain.view.ExploreFiltersView;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExploreFiltersReader {

	private final ExploreQueryRepository exploreQueryRepository;

	@Cacheable(cacheNames = CacheNames.EXPLORE_FILTERS, key = "'v1'")
	public ExploreFiltersView getFilters() {
		return exploreQueryRepository.getFilters();
	}
}
