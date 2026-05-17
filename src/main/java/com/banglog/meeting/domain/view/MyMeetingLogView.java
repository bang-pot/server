package com.banglog.meeting.domain.view;

import java.time.Instant;
import java.util.List;

public record MyMeetingLogView(
	Status status,
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

	public enum Status {
		EXISTS,
		NOT_WRITTEN,
		DELETED_BLOCKED
	}

	public static MyMeetingLogView of(
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
		return new MyMeetingLogView(
			Status.EXISTS,
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

	public static MyMeetingLogView notWritten() {
		return missing(Status.NOT_WRITTEN);
	}

	public static MyMeetingLogView deletedBlocked() {
		return missing(Status.DELETED_BLOCKED);
	}

	private static MyMeetingLogView missing(Status status) {
		return new MyMeetingLogView(status, null, null, null, null, null, null, null, null, null, null, List.of());
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
