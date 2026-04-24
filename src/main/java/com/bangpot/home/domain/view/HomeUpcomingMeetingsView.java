package com.bangpot.home.domain.view;

import java.util.List;

public record HomeUpcomingMeetingsView(
	List<HomeUpcomingMeetingsView.Item> items,
	Long totalCount
) {
	public static HomeUpcomingMeetingsView of(List<HomeUpcomingMeetingsView.Item> items, Long totalCount) {
		return new HomeUpcomingMeetingsView(items, totalCount);
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
