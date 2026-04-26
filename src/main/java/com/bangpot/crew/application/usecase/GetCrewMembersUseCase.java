package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.view.CrewMembersView;

public interface GetCrewMembersUseCase {

	CrewMembersView handle(Query query);

	record Query(Long crewId, Long userId) {

		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

}
