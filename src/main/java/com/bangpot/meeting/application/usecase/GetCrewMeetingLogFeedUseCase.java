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
		String authorNickname,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long extraPhotoCount
	) {
		public static Item of(
			Long logId,
			Long meetingId,
			String authorNickname,
			String meetingTitle,
			String meetingDate,
			Instant createdAt,
			String excerpt,
			String coverPhotoUrl,
			Long extraPhotoCount
		) {
			return new Item(
				logId,
				meetingId,
				authorNickname,
				meetingTitle,
				meetingDate,
				createdAt,
				excerpt,
				coverPhotoUrl,
				extraPhotoCount
			);
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
