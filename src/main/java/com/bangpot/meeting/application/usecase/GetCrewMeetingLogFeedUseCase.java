package com.bangpot.meeting.application.usecase;

import java.time.Instant;
import java.util.List;

public interface GetCrewMeetingLogFeedUseCase {

	Result handle(Query query);

	record Query(
		Long crewId,
		Long userId,
		int page,
		int size
	) {
		public static Query of(Long crewId, Long userId, int page, int size) {
			return new Query(crewId, userId, page, size);
		}
	}

	record Result(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static Result of(List<Item> items, PageInfo pageInfo) {
			return new Result(items, pageInfo);
		}
	}

	record Item(
		Long logId,
		Long meetingId,
		Long crewId,
		String authorNickname,
		String meetingTitle,
		String themeName,
		String date,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long photoCount
	) {
		public static Item of(
			Long logId,
			Long meetingId,
			Long crewId,
			String authorNickname,
			String meetingTitle,
			String themeName,
			String date,
			Instant createdAt,
			String excerpt,
			String coverPhotoUrl,
			Long photoCount
		) {
			return new Item(logId, meetingId, crewId, authorNickname, meetingTitle, themeName, date, createdAt, excerpt,
				coverPhotoUrl, photoCount);
		}
	}

	record PageInfo(
		int page,
		int size,
		boolean hasNext
	) {
		public static PageInfo of(int page, int size, boolean hasNext) {
			return new PageInfo(page, size, hasNext);
		}
	}
}
