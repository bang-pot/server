package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.CrewJoinViewStatus;

public interface GetCrewJoinViewUseCase {

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
		CrewJoinViewStatus myStatus
	) {
		public static Result of(
			Long crewId,
			String name,
			String description,
			String visibility,
			String imageUrl,
			CrewJoinViewStatus myStatus
		) {
			return new Result(crewId, name, description, visibility, imageUrl, myStatus);
		}
	}
}
