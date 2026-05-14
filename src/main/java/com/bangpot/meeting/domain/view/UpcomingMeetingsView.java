package com.bangpot.meeting.domain.view;

import java.util.List;

public record UpcomingMeetingsView(
	UpcomingMeetingsView.Item nearestMeeting,
	Long totalCount
) {
	public static UpcomingMeetingsView of(List<UpcomingMeetingsView.Item> items, Long totalCount) {
		return new UpcomingMeetingsView(items.isEmpty() ? null : items.getFirst(), totalCount);
	}

	public static UpcomingMeetingsView of(UpcomingMeetingsView.Item nearestMeeting, Long totalCount) {
		return new UpcomingMeetingsView(nearestMeeting, totalCount);
	}

	public List<UpcomingMeetingsView.Item> items() {
		if (nearestMeeting == null) {
			return List.of();
		}
		return List.of(nearestMeeting);
	}

	public record Item(
		Long meetingId,
		String themeName,
		String date,
		String time
	) {
		public static Item of(
			Long meetingId,
			String themeName,
			String date,
			String time
		) {
			return new Item(meetingId, themeName, date, time);
		}
	}
}
