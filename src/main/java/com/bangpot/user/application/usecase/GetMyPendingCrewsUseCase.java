package com.bangpot.user.application.usecase;

import com.bangpot.crew.domain.view.MyPendingCrewsView;

public interface GetMyPendingCrewsUseCase {

	MyPendingCrewsView handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
		}
	}
}
