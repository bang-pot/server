package com.banglog.meeting.domain.view;

import java.time.Instant;
import java.util.List;

public record CrewMeetingLogFeedView(
	List<CrewMeetingLogFeedView.Item> items,
	CrewMeetingLogFeedView.Page page
) {

	public static CrewMeetingLogFeedView of(List<CrewMeetingLogFeedView.Item> items, CrewMeetingLogFeedView.Page page) {
		return new CrewMeetingLogFeedView(items, page);
	}

	public record Item(
		Long logId,
		Long meetingId,
		String authorNickname,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long extraPhotoCount
	) {

		public static Item of(
			Long logId,
			Long meetingId,
			String authorNickname,
			String meetingTitle,
			String meetingDate,
			Instant createdAt,
			String excerpt,
			String coverPhotoUrl,
			Long extraPhotoCount
		) {
			return new Item(
				logId,
				meetingId,
				authorNickname,
				meetingTitle,
				meetingDate,
				createdAt,
				excerpt,
				coverPhotoUrl,
				extraPhotoCount
			);
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
