package com.banglog.meeting.application.exception;

public class MeetingLogAlreadyExistsException extends RuntimeException {

	public MeetingLogAlreadyExistsException(Long meetingId, Long authorUserId) {
		super("이미 작성한 방탈로그가 있습니다. meetingId=" + meetingId + ", authorUserId=" + authorUserId);
	}

	public MeetingLogAlreadyExistsException(Long meetingId, Long authorUserId, Throwable cause) {
		super("이미 작성한 방탈로그가 있습니다. meetingId=" + meetingId + ", authorUserId=" + authorUserId, cause);
	}
}
