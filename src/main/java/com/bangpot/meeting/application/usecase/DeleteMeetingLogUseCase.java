package com.bangpot.meeting.application.usecase;

public interface DeleteMeetingLogUseCase {

	Result handle(Command command);

	record Command(
		Long logId,
		Long userId
	) {
		public static Command of(Long logId, Long userId) {
			return new Command(logId, userId);
		}
	}

	record Result(Long logId) {
		public static Result of(Long logId) {
			return new Result(logId);
		}
	}
}

