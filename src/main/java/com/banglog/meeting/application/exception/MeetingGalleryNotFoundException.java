package com.banglog.meeting.application.exception;

public class MeetingGalleryNotFoundException extends RuntimeException {

	public MeetingGalleryNotFoundException(Long meetingId) {
		super("meeting gallery not found: " + meetingId);
	}
}
