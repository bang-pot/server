package com.bangpot.explore.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.bangpot.common.cache.CacheNames;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ExploreFiltersView;

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
