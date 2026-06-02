package com.banglog.meeting.domain.view;

import java.time.Instant;
import java.util.List;

public record MyMeetingLogsView(
	List<MyMeetingLogsView.Item> items,
	MyMeetingLogsView.Page page
) {
	public static MyMeetingLogsView of(List<MyMeetingLogsView.Item> items, MyMeetingLogsView.Page page) {
		return new MyMeetingLogsView(items, page);
	}

	public record Item(
		Long logId,
		Long crewId,
		String crewName,
		Long meetingId,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		com.banglog.meeting.domain.MeetingResult result,
		String excerpt,
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
			com.banglog.meeting.domain.MeetingResult result,
			String excerpt,
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
				result,
				excerpt,
				coverPhotoUrl,
				photoCount
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
