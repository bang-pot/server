package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetMeetingCreateCrewsUseCase {

	Result handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record CrewItem(Long crewId, String crewName) {

		public static CrewItem of(Long crewId, String crewName) {
			return new CrewItem(crewId, crewName);
		}
	}

	record Result(List<CrewItem> crews) {

		public static Result of(List<CrewItem> crews) {
			return new Result(crews);
		}
	}
}
