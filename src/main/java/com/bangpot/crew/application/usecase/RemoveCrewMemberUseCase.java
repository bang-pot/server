package com.bangpot.crew.application.usecase;

public interface RemoveCrewMemberUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long leaderUserId, Long targetUserId) {

		public static Command of(Long crewId, Long leaderUserId, Long targetUserId) {
			return new Command(crewId, leaderUserId, targetUserId);
		}
	}

	record Result(Long crewId, Long removedUserId) {

		public static Result of(Long crewId, Long removedUserId) {
			return new Result(crewId, removedUserId);
		}
	}
}
