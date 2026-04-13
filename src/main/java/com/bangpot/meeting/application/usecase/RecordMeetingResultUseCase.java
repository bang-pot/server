package com.bangpot.meeting.application.usecase;

public interface RecordMeetingResultUseCase {

	Result handle(Command command);

	record Command(
		Long crewId,
		Long meetingId,
		Long userId,
		String result
	) {
		public static Command of(Long crewId, Long meetingId, Long userId, String result) {
			return new Command(crewId, meetingId, userId, result);
		}
	}

	record Result(
		Long meetingId,
		String result
	) {
		public static Result of(Long meetingId, String result) {
			return new Result(meetingId, result);
		}
	}
}
