package com.banglog.meeting.application.exception;

public class MeetingInvalidStatusTransitionException extends RuntimeException {

	public MeetingInvalidStatusTransitionException(Long meetingId, String fromStatus, String targetStatus) {
		super("허용되지 않은 모임 상태 전이입니다. meetingId=%d, from=%s, to=%s"
			.formatted(meetingId, fromStatus, targetStatus));
	}
}
