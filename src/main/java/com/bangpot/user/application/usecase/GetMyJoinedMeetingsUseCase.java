package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyJoinedMeetingsUseCase {

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
		String themeName,
		Long crewId,
		String crewName,
		String date,
		String time,
		String status,
		String result,
		boolean canWriteReview
	) {
		public static Item of(
			Long meetingId,
			String title,
			String themeName,
			Long crewId,
			String crewName,
			String date,
			String time,
			String status,
			String result,
			boolean canWriteReview
		) {
			return new Item(
				meetingId,
				title,
				themeName,
				crewId,
				crewName,
				date,
				time,
				status,
				result,
				canWriteReview
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
