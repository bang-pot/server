package com.bangpot.explore.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreThemesService implements GetExploreThemesUseCase {

	private final ExploreThemeReadRepository exploreThemeReadRepository;

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

		return Result.of(result.items(), result.pageInfo());
	}
}
