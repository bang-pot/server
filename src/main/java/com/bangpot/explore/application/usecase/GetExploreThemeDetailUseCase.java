package com.bangpot.explore.application.usecase;

import com.bangpot.explore.domain.view.ExploreThemeDetailView;

public interface GetExploreThemeDetailUseCase {

	ExploreThemeDetailView handle(Query query);

	record Query(Long userId, Long themeId) {
		public static Query of(Long userId, Long themeId) {
			return new Query(userId, themeId);
		}

		public static Query of(Long themeId) {
			return of(null, themeId);
		}
	}
}
