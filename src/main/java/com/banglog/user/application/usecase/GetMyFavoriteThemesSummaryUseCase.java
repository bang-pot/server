package com.banglog.user.application.usecase;

import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;

public interface GetMyFavoriteThemesSummaryUseCase {

	MyFavoriteThemesSummaryView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
