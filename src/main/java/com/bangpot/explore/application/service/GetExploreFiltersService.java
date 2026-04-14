package com.bangpot.explore.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreFiltersService implements GetExploreFiltersUseCase {

	private final ExploreThemeReadRepository exploreThemeReadRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle() {
		return exploreThemeReadRepository.getFilters();
	}
}
