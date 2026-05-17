package com.banglog.meeting.domain.view;

import java.util.List;

public record MeetingsView(
	List<MeetingsView.Item> items,
	MeetingsView.Page page
) {

	public static MeetingsView of(List<MeetingsView.Item> items) {
		return new MeetingsView(items, Page.of(0, items.size(), false));
	}

	public static MeetingsView of(List<MeetingsView.Item> items, MeetingsView.Page page) {
		return new MeetingsView(items, page);
	}

	public record Item(
		Long meetingId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		String status,
		String result,
		Integer capacity
	) {

		public static Item of(
			Long meetingId,
			String title,
			String themeName,
			String place,
			String date,
			String time,
			String status,
			String result,
			Integer capacity
		) {
			return new Item(meetingId, title, themeName, place, date, time, status, result, capacity);
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
