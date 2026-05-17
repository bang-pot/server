package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.PendingCrewJoinRequestsView;

public interface GetPendingCrewJoinRequestsUseCase {

	PendingCrewJoinRequestsView handle(Query query);

	record Query(Long crewId, Long leaderUserId, int page, int size) {
		public static Query of(Long crewId, Long leaderUserId, int page, int size) {
			return new Query(crewId, leaderUserId, page, size);
		}
	}
}
