package com.bangpot.meeting.application.exception;

public class MeetingLogWriteNotAllowedException extends RuntimeException {

	public MeetingLogWriteNotAllowedException(Long meetingId) {
		super("완료된 모임에만 방탈로그를 작성할 수 있습니다. meetingId=" + meetingId);
	}
}
