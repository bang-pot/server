package com.bangpot.meeting.application.exception;

public class MeetingEditNotAllowedException extends RuntimeException {

	public MeetingEditNotAllowedException(Long meetingId, String status) {
		super("모집중인 모임만 수정할 수 있습니다. meetingId=%d, status=%s".formatted(meetingId, status));
	}
}
