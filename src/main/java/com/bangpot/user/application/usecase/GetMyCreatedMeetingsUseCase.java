package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyCreatedMeetingsUseCase {

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
		Long meetingId,
		String title,
		String status,
		String date,
		String time,
		Long crewId,
		String crewName
	) {
		public static Item of(
			Long meetingId,
			String title,
			String status,
			String date,
			String time,
			Long crewId,
			String crewName
		) {
			return new Item(meetingId, title, status, date, time, crewId, crewName);
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
