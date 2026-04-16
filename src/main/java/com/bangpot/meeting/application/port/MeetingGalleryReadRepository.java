package com.bangpot.meeting.application.port;

import java.util.List;

public interface MeetingGalleryReadRepository {

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
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		String coverPhotoUrl,
		Long extraPhotoCount
	) {
		public static Item of(
			Long meetingId,
			String meetingDate,
			String meetingTitle,
			String coverPhotoUrl,
			Long extraPhotoCount
		) {
			return new Item(meetingId, meetingDate, meetingTitle, coverPhotoUrl, extraPhotoCount);
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
