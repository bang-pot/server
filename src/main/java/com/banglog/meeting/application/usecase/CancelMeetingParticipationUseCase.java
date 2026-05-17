package com.banglog.meeting.application.usecase;

public interface CancelMeetingParticipationUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long meetingId, Long userId) {
		public static Command of(Long crewId, Long meetingId, Long userId) {
			return new Command(crewId, meetingId, userId);
		}
	}

	record Result(Long meetingId, String myParticipationStatus) {
		public static Result of(Long meetingId, String myParticipationStatus) {
			return new Result(meetingId, myParticipationStatus);
		}
	}
}
