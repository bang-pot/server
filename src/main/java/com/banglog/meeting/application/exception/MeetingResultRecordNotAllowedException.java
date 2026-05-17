package com.banglog.meeting.application.exception;

public class MeetingResultRecordNotAllowedException extends RuntimeException {

	public MeetingResultRecordNotAllowedException(Long meetingId, String status) {
		super("모임 결과를 기록할 수 없는 상태입니다. meetingId=%d, status=%s".formatted(meetingId, status));
	}
}
