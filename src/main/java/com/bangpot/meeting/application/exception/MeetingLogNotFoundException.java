package com.bangpot.meeting.application.exception;

public class MeetingLogNotFoundException extends RuntimeException {

	public MeetingLogNotFoundException(Long logId) {
		super("meeting log not found: " + logId);
	}
}

