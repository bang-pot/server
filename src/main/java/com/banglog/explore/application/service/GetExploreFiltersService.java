package com.banglog.explore.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.explore.application.usecase.GetExploreFiltersUseCase;
import com.banglog.explore.domain.view.ExploreFiltersView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreFiltersService implements GetExploreFiltersUseCase {

	private final ExploreFiltersReader exploreFiltersReader;

	@Override
	@Transactional(readOnly = true)
	public ExploreFiltersView handle() {
		return exploreFiltersReader.getFilters();
	}
}
