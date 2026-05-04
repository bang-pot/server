package com.bangpot.meeting.domain.view;

import java.time.Instant;
import java.util.List;

public record MeetingLogDetailView(
	Long logId,
	Long meetingId,
	String meetingTitle,
	String themeName,
	String place,
	String date,
	String authorNickname,
	Instant createdAt,
	Instant updatedAt,
	String body,
	List<String> photos
) {

	public static MeetingLogDetailView of(
		Long logId,
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String authorNickname,
		Instant createdAt,
		Instant updatedAt,
		String body,
		List<String> photos
	) {
		return new MeetingLogDetailView(
			logId,
			meetingId,
			meetingTitle,
			themeName,
			place,
			date,
			authorNickname,
			createdAt,
			updatedAt,
			body,
			photos
		);
	}

	public record Source(
		Long logId,
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String authorNickname,
		Instant createdAt,
		Instant updatedAt,
		String body
	) {
	}
}
