package com.bangpot.meeting.domain.view;

import java.util.List;

public record CrewMeetingGalleryView(
	List<CrewMeetingGalleryView.Item> items,
	CrewMeetingGalleryView.Page page
) {

	public static CrewMeetingGalleryView of(List<CrewMeetingGalleryView.Item> items, CrewMeetingGalleryView.Page page) {
		return new CrewMeetingGalleryView(items, page);
	}

	public record Item(
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

	public record Page(
		int page,
		int size,
		boolean hasNext
	) {

		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
