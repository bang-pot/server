package com.bangpot.meeting.application.usecase;

import java.time.Instant;
import java.util.List;

public interface GetMyMeetingLogUseCase {

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
	}
}

