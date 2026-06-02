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
		String themeName,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long extraPhotoCount,
		String result
	) {

		public static Item of(
			Long logId,
			Long meetingId,
			String authorNickname,
			String meetingTitle,
			String themeName,
			String meetingDate,
			Instant createdAt,
			String excerpt,
			String coverPhotoUrl,
			Long extraPhotoCount,
			String result
		) {
			return new Item(
				logId,
				meetingId,
				authorNickname,
				meetingTitle,
				themeName,
				meetingDate,
				createdAt,
				excerpt,
				coverPhotoUrl,
				extraPhotoCount,
				result
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
