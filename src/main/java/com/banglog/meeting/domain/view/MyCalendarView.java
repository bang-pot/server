package com.banglog.meeting.domain.view;

import java.util.List;

public record MyCalendarView(
	List<MyCalendarView.Item> items,
	int totalCount
) {
	public static MyCalendarView of(List<MyCalendarView.Item> items, int totalCount) {
		return new MyCalendarView(items, totalCount);
	}

	public record Item(
		Long meetingId,
		String meetingTitle,
		Long crewId,
		String crewName,
		String date,
		String time,
		String meetingStatus,
		boolean isCanceled,
		String participationRole
	) {
		public static Item of(
			Long meetingId,
			String meetingTitle,
			Long crewId,
			String crewName,
			String date,
			String time,
			String meetingStatus,
			boolean isCanceled,
			String participationRole
		) {
			return new Item(
				meetingId,
				meetingTitle,
				crewId,
				crewName,
				date,
				time,
				meetingStatus,
				isCanceled,
				participationRole
			);
		}
	}
}
