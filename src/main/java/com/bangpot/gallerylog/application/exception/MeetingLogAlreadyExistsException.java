package com.bangpot.gallerylog.application.exception;

public class MeetingLogAlreadyExistsException extends RuntimeException {

	public MeetingLogAlreadyExistsException(Long meetingId, Long authorUserId) {
		super("meeting log already exists: meetingId=" + meetingId + ", authorUserId=" + authorUserId);
	}
}
