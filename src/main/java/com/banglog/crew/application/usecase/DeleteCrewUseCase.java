package com.banglog.crew.application.usecase;

public interface DeleteCrewUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long leaderUserId, String crewName) {

		public static Command of(Long crewId, Long leaderUserId, String crewName) {
			return new Command(crewId, leaderUserId, crewName);
		}
	}

	record Result(Long crewId) {

		public static Result of(Long crewId) {
			return new Result(crewId);
		}
	}
}
