package com.bangpot.meeting.application.exception;

public class MeetingParticipationAlreadyApprovedException extends RuntimeException {

	public MeetingParticipationAlreadyApprovedException(Long meetingId, Long userId) {
		super("Meeting participation already approved: meetingId=%d, userId=%d".formatted(meetingId, userId));
	}
}
