package com.bangpot.meeting.application.usecase;

import java.util.List;

public interface GetArchivedMeetingsUseCase {

	Result handle(Query query);

	record Query(
		Long userId,
		int page,
		int size
	) {
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
		Long crewId,
		String crewName,
		String themeName,
		String place,
		String date,
		String result,
		String posterImageUrl
	) {
		public static Item of(
			Long meetingId,
			Long crewId,
			String crewName,
			String themeName,
			String place,
			String date,
			String result,
			String posterImageUrl
		) {
			return new Item(meetingId, crewId, crewName, themeName, place, date, result, posterImageUrl);
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

