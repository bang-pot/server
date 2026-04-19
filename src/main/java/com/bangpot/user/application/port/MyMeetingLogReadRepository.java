package com.bangpot.user.application.port;

import java.time.Instant;
import java.util.List;

public interface MyMeetingLogReadRepository {

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
		Long logId,
		Long crewId,
		String crewName,
		Long meetingId,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String body,
		String coverPhotoUrl,
		Long photoCount
	) {
		public static Item of(
			Long logId,
			Long crewId,
			String crewName,
			Long meetingId,
			String meetingTitle,
			String meetingDate,
			Instant createdAt,
			String body,
			String coverPhotoUrl,
			Long photoCount
		) {
			return new Item(
				logId,
				crewId,
				crewName,
				meetingId,
				meetingTitle,
				meetingDate,
				createdAt,
				body,
				coverPhotoUrl,
				photoCount
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
