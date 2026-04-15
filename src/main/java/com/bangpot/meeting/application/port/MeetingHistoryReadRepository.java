package com.bangpot.meeting.application.port;

import java.util.List;

public interface MeetingHistoryReadRepository {

	SearchResult search(Long crewId, Long userId, int page, int size);

	record SearchResult(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static SearchResult of(List<Item> items, PageInfo pageInfo) {
			return new SearchResult(items, pageInfo);
		}
	}

	record Item(
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String result,
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
