package com.bangpot.meeting.application.usecase;

public interface DeleteMeetingLogUseCase {

	Result handle(Command command);

	enum DeletedBy {
		AUTHOR,
		LEADER
	}

	record Command(
		Long crewId,
		Long logId,
		Long userId,
		String deleteReason
	) {
		public static Command of(Long crewId, Long logId, Long userId, String deleteReason) {
			return new Command(crewId, logId, userId, deleteReason);
		}
	}

	record Result(Long logId, DeletedBy deletedBy) {
		public static Result of(Long logId, DeletedBy deletedBy) {
			return new Result(logId, deletedBy);
		}
	}
}
