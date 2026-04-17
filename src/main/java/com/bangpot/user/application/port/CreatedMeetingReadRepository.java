package com.bangpot.user.application.port;

import java.util.List;

public interface CreatedMeetingReadRepository {

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
