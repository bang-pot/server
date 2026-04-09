package com.bangpot.crew.application.usecase;

public interface RejectCrewJoinRequestUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long requestId, Long leaderUserId) {
		public static Command of(Long crewId, Long requestId, Long leaderUserId) {
			return new Command(crewId, requestId, leaderUserId);
		}
	}

	record Result(Long crewId, Long requestId) {
		public static Result of(Long crewId, Long requestId) {
			return new Result(crewId, requestId);
		}
	}
}
