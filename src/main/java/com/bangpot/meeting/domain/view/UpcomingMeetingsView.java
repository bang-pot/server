package com.bangpot.meeting.domain.view;

import java.util.List;

public record UpcomingMeetingsView(
	List<UpcomingMeetingsView.Item> items,
	Long totalCount
) {
	public static UpcomingMeetingsView of(List<UpcomingMeetingsView.Item> items, Long totalCount) {
		return new UpcomingMeetingsView(items, totalCount);
	}

	public record Item(
		Long meetingId,
		String title,
		Long crewId,
		String crewName,
		String date,
		String time,
		String status
	) {
		public static Item of(
			Long meetingId,
			String title,
			Long crewId,
			String crewName,
			String date,
			String time,
			String status
		) {
			return new Item(meetingId, title, crewId, crewName, date, time, status);
		}
	}
}
