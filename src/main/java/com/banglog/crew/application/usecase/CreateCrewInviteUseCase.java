package com.banglog.crew.application.usecase;

public interface CreateCrewInviteUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long inviterUserId, Long targetUserId) {
		public static Command of(Long crewId, Long inviterUserId, Long targetUserId) {
			return new Command(crewId, inviterUserId, targetUserId);
		}
	}

	record Result(Long crewId, Long targetUserId, String status) {
		public static Result of(Long crewId, Long targetUserId, String status) {
			return new Result(crewId, targetUserId, status);
		}
	}
}
