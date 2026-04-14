package com.bangpot.meeting.application.exception;

public class MeetingLogWriteNotAllowedException extends RuntimeException {

	public MeetingLogWriteNotAllowedException(Long meetingId) {
		super("meeting log write not allowed: meetingId=" + meetingId);
	}
}

