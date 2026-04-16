package com.bangpot.meeting.application.port;

import java.util.List;
import java.util.Optional;

public interface MeetingGalleryReadRepository {

	SearchResult search(Long crewId, int page, int size);

	Optional<Detail> findDetail(Long crewId, Long meetingId);

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

	record Detail(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		List<DetailPhoto> photos,
		int totalPhotoCount
	) {
		public static Detail of(
			Long meetingId,
			String meetingDate,
			String meetingTitle,
			List<DetailPhoto> photos,
			int totalPhotoCount
		) {
			return new Detail(meetingId, meetingDate, meetingTitle, photos, totalPhotoCount);
		}
	}

	record DetailPhoto(
		Long photoId,
		String url,
		int order
	) {
		public static DetailPhoto of(Long photoId, String url, int order) {
			return new DetailPhoto(photoId, url, order);
		}
	}
}
