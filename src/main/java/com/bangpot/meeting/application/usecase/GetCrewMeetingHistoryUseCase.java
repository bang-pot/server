package com.bangpot.meeting.application.usecase;

import java.util.List;

public interface GetCrewMeetingHistoryUseCase {

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
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String result,
		String myLogStatus,
		Long logId
	) {
		public static Item of(
			Long meetingId,
			String meetingTitle,
			String themeName,
			String place,
			String date,
			String result,
			String myLogStatus,
			Long logId
		) {
			return new Item(meetingId, meetingTitle, themeName, place, date, result, myLogStatus, logId);
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
