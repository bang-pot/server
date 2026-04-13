package com.bangpot.meeting.application.exception;

public class MeetingNotFoundException extends RuntimeException {

	public MeetingNotFoundException(Long meetingId) {
		super("모임을 찾을 수 없습니다. meetingId=" + meetingId);
	}
}
