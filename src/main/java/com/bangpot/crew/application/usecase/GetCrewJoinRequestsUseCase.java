package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.view.CrewJoinRequestsView;

public interface GetCrewJoinRequestsUseCase {

	CrewJoinRequestsView handle(Query query);

	record Query(Long crewId, Long leaderUserId, int page, int size) {
		public static Query of(Long crewId, Long leaderUserId, int page, int size) {
			return new Query(crewId, leaderUserId, page, size);
		}
	}
}
