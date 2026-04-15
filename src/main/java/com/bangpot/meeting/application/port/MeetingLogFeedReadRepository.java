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
		String authorNickname,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String body,
		String coverPhotoUrl,
		Long totalPhotoCount
	) {
		public static Item of(
			Long logId,
			Long meetingId,
			String authorNickname,
			String meetingTitle,
			String meetingDate,
			Instant createdAt,
			String body,
			String coverPhotoUrl,
			Long totalPhotoCount
		) {
			return new Item(
				logId,
				meetingId,
				authorNickname,
				meetingTitle,
				meetingDate,
				createdAt,
				body,
				coverPhotoUrl,
				totalPhotoCount
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
