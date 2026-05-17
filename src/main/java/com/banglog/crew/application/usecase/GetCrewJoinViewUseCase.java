package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.CrewJoinView;

public interface GetCrewJoinViewUseCase {

	CrewJoinView handle(Query query);

	record Query(Long crewId, Long userId) {
		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

}
