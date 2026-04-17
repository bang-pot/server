package com.bangpot.user.application.port;

import java.util.List;

public interface JoinedMeetingReadRepository {

	SearchResult search(Long userId, int page, int size);

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
