package com.bangpot.user.application.usecase;

public interface CancelMyPendingCrewJoinRequestUseCase {

	Result handle(Command command);

	record Command(Long userId, Long joinRequestId) {
		public static Command of(Long userId, Long joinRequestId) {
			return new Command(userId, joinRequestId);
		}
	}

	record Result(
		Long joinRequestId,
		Long crewId
	) {
		public static Result of(Long joinRequestId, Long crewId) {
			return new Result(joinRequestId, crewId);
		}
	}
}
