package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.CrewJoinViewStatus;

public interface RequestCrewJoinUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long userId, String message) {
		public static Command of(Long crewId, Long userId, String message) {
			return new Command(crewId, userId, message);
		}
	}

	record Result(Long crewId, CrewJoinViewStatus myStatus) {
		public static Result of(Long crewId, CrewJoinViewStatus myStatus) {
			return new Result(crewId, myStatus);
		}
	}
}
