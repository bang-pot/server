package com.bangpot.meeting.application.usecase;

import java.time.Instant;
import java.util.List;

public interface GetMyMeetingLogUseCase {

	enum Status {
		EXISTS,
		NOT_WRITTEN,
		DELETED_BLOCKED
	}

	Result handle(Query query);

	record Query(
		Long meetingId,
		Long userId
	) {
		public static Query of(Long meetingId, Long userId) {
			return new Query(meetingId, userId);
		}
	}

	record Result(
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
		public static Result of(
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
			return new Result(
				status,
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

		public static Result notWritten() {
			return new Result(Status.NOT_WRITTEN, null, null, null, null, null, null, null, null, null, null, List.of());
		}

		public static Result deletedBlocked() {
			return new Result(Status.DELETED_BLOCKED, null, null, null, null, null, null, null, null, null, null, List.of());
		}
	}
}

