package com.banglog.meeting.domain.view;

public record CrewMeetingGalleryDetailTargetView(
	Long meetingId,
	String meetingDate,
	String meetingTitle
) {

	public static CrewMeetingGalleryDetailTargetView of(Long meetingId, String meetingDate, String meetingTitle) {
		return new CrewMeetingGalleryDetailTargetView(meetingId, meetingDate, meetingTitle);
	}
}
