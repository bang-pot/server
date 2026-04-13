package com.bangpot.crew.application.usecase;

public interface LeaveCrewUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long userId) {

		public static Command of(Long crewId, Long userId) {
			return new Command(crewId, userId);
		}
	}

	record Result(Long crewId) {

		public static Result of(Long crewId) {
			return new Result(crewId);
		}
	}
}
