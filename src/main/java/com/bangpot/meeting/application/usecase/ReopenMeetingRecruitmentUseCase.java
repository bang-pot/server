package com.bangpot.meeting.application.usecase;

public interface ReopenMeetingRecruitmentUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long meetingId, Long userId) {
		public static Command of(Long crewId, Long meetingId, Long userId) {
			return new Command(crewId, meetingId, userId);
		}
	}

	record Result(Long meetingId, String status) {
		public static Result of(Long meetingId, String status) {
			return new Result(meetingId, status);
		}
	}
}
