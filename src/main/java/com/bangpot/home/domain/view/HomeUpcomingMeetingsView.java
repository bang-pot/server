package com.bangpot.home.domain.view;

public record HomeUpcomingMeetingsView(
	HomeUpcomingMeetingsView.Item nearestMeeting,
	Long totalCount
) {
	public static HomeUpcomingMeetingsView of(HomeUpcomingMeetingsView.Item nearestMeeting, Long totalCount) {
		return new HomeUpcomingMeetingsView(nearestMeeting, totalCount);
	}

	public static HomeUpcomingMeetingsView empty() {
		return new HomeUpcomingMeetingsView(null, 0L);
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
