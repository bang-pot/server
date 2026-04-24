package com.bangpot.explore.application.usecase;

import java.util.List;

import com.bangpot.explore.domain.view.ExploreThemeSearchView;

public interface GetExploreThemesUseCase {

	ExploreThemeSearchView handle(Query query);

	record Query(
		Long userId,
		String keyword,
		List<String> genres,
		String region,
		String district,
		int page,
		int size
	) {
		public static Query of(
			Long userId,
			String keyword,
			List<String> genres,
			String region,
			String district,
			int page,
			int size
		) {
			return new Query(userId, keyword, genres, region, district, page, size);
		}

		public static Query of(
			String keyword,
			List<String> genres,
			String region,
			String district,
			int page,
			int size
		) {
			return of(null, keyword, genres, region, district, page, size);
		}
	}
}
