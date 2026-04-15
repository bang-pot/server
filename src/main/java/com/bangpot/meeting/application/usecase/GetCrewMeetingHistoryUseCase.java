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
		Long logId,
		String reviewSummary,
		Long logCount,
		Long participantCount,
		String coverPhotoUrl
	) {
		public static Item of(
			Long meetingId,
			String meetingTitle,
			String themeName,
			String place,
			String date,
			String result,
			String myLogStatus,
			Long logId,
			String reviewSummary,
			Long logCount,
			Long participantCount,
			String coverPhotoUrl
		) {
			return new Item(
				meetingId,
				meetingTitle,
				themeName,
				place,
				date,
				result,
				myLogStatus,
				logId,
				reviewSummary,
				logCount,
				participantCount,
				coverPhotoUrl
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
