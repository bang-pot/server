package com.bangpot.meeting.application.exception;

public class MeetingParticipationAlreadyPendingException extends RuntimeException {

	public MeetingParticipationAlreadyPendingException(Long meetingId, Long userId) {
		super("Meeting participation already pending: meetingId=%d, userId=%d".formatted(meetingId, userId));
	}
}
