package com.bangpot.user.application.usecase;

import java.time.Instant;
import java.util.List;

public interface GetMyMeetingLogsUseCase {

	Result handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
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
		Long crewId,
		String crewName,
		Long meetingId,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long photoCount
	) {
		public static Item of(
			Long logId,
			Long crewId,
			String crewName,
			Long meetingId,
			String meetingTitle,
			String meetingDate,
			Instant createdAt,
			String excerpt,
			String coverPhotoUrl,
			Long photoCount
		) {
			return new Item(
				logId,
				crewId,
				crewName,
				meetingId,
				meetingTitle,
				meetingDate,
				createdAt,
				excerpt,
				coverPhotoUrl,
				photoCount
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
