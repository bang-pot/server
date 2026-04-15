package com.bangpot.meeting.application.port;

import java.time.Instant;
import java.util.List;

public interface MeetingLogFeedReadRepository {

	SearchResult search(Long crewId, int page, int size);

	record SearchResult(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static SearchResult of(List<Item> items, PageInfo pageInfo) {
			return new SearchResult(items, pageInfo);
		}
	}

	record Item(
		Long logId,
		Long meetingId,
		Long crewId,
		String authorNickname,
		String meetingTitle,
		String themeName,
		String date,
		Instant createdAt,
		String body,
		String coverPhotoUrl,
		Long photoCount
	) {
		public static Item of(
			Long logId,
			Long meetingId,
			Long crewId,
			String authorNickname,
			String meetingTitle,
			String themeName,
			String date,
			Instant createdAt,
			String body,
			String coverPhotoUrl,
			Long photoCount
		) {
			return new Item(logId, meetingId, crewId, authorNickname, meetingTitle, themeName, date, createdAt, body,
				coverPhotoUrl, photoCount);
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
