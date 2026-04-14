package com.bangpot.archive.application.port;

import java.util.List;

public interface ArchiveMeetingReadRepository {

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
		Long crewId,
		String crewName,
		String themeName,
		String place,
		String date,
		String result
	) {
		public static Item of(
			Long meetingId,
			Long crewId,
			String crewName,
			String themeName,
			String place,
			String date,
			String result
		) {
			return new Item(meetingId, crewId, crewName, themeName, place, date, result);
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
