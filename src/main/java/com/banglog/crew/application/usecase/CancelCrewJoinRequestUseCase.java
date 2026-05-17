package com.banglog.crew.application.usecase;

public interface CancelCrewJoinRequestUseCase {

	Result handle(Command command);

	record Command(Long userId, Long requestId) {
		public static Command of(Long userId, Long requestId) {
			return new Command(userId, requestId);
		}
	}

	record Result(Long requestId, Long crewId) {
		public static Result of(Long requestId, Long crewId) {
			return new Result(requestId, crewId);
		}
	}
}
