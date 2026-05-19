package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.CrewMembersView;

public interface GetCrewMembersUseCase {

	CrewMembersView handle(Query query);

	record Query(Long crewId, Long userId, int page, int size) {

		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId, 0, 20);
		}

		public static Query of(Long crewId, Long userId, int page, int size) {
			return new Query(crewId, userId, page, size);
		}
	}

}
