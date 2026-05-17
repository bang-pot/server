package com.banglog.home.application.usecase;

import com.banglog.home.domain.view.HomeView;

public interface GetHomeUseCase {

	HomeView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
