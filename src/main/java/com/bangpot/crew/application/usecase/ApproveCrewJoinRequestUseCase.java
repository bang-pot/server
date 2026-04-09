package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.CrewRole;

public interface ApproveCrewJoinRequestUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long requestId, Long leaderUserId) {
		public static Command of(Long crewId, Long requestId, Long leaderUserId) {
			return new Command(crewId, requestId, leaderUserId);
		}
	}

	record Result(Long crewId, Long requestId, Long userId, CrewRole role) {
		public static Result of(Long crewId, Long requestId, Long userId, CrewRole role) {
			return new Result(crewId, requestId, userId, role);
		}
	}
}
