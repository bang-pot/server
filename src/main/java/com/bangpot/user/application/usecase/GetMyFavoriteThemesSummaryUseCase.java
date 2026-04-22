package com.bangpot.user.application.usecase;

import com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView;

public interface GetMyFavoriteThemesSummaryUseCase {

	MyFavoriteThemesSummaryView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
