package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.MyCrewInvitesView;

public interface GetMyCrewInvitesUseCase {

	MyCrewInvitesView handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
		}
	}

}
