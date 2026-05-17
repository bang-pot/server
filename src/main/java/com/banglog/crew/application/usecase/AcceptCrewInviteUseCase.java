package com.banglog.crew.application.usecase;

public interface AcceptCrewInviteUseCase {

	Result handle(Command command);

	record Command(Long inviteId, Long userId) {
		public static Command of(Long inviteId, Long userId) {
			return new Command(inviteId, userId);
		}
	}

	record Result(Long inviteId, Long crewId, String status) {
		public static Result of(Long inviteId, Long crewId, String status) {
			return new Result(inviteId, crewId, status);
		}
	}
}
