package com.bangpot.meeting.application.exception;

public class MeetingNotFoundException extends RuntimeException {

	public MeetingNotFoundException(Long meetingId) {
		super("meeting not found. meetingId=" + meetingId);
	}
}
