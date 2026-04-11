package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.CrewRole;

public interface GetCrewHubUseCase {

	Result handle(Query query);

	record Query(Long crewId, Long userId) {

		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

	record Result(
		Long crewId,
		String name,
		String description,
		String visibility,
		String imageUrl,
		CrewRole myRole,
		boolean hasNotice,
		Integer pendingJoinRequestCount
	) {

		public static Result of(
			Long crewId,
			String name,
			String description,
			String visibility,
			String imageUrl,
			CrewRole myRole,
			boolean hasNotice,
			Integer pendingJoinRequestCount
		) {
			return new Result(crewId, name, description, visibility, imageUrl, myRole, hasNotice, pendingJoinRequestCount);
		}
	}
}
