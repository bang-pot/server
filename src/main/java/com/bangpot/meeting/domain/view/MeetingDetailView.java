package com.bangpot.meeting.domain.view;

public record MeetingDetailView(
	Long meetingId,
	Long crewId,
	Long hostUserId,
	String title,
	String themeName,
	String place,
	String date,
	String time,
	Integer capacity,
	Integer totalCost,
	String contactLink,
	String description,
	String status,
	String result,
	String myParticipationStatus
) {

	public static MeetingDetailView of(
		Long meetingId,
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description,
		String status,
		String result,
		String myParticipationStatus
	) {
		return new MeetingDetailView(
			meetingId,
			crewId,
			hostUserId,
			title,
			themeName,
			place,
			date,
			time,
			capacity,
			totalCost,
			contactLink,
			description,
			status,
			result,
			myParticipationStatus
		);
	}
}
