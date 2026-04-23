package com.bangpot.meeting.domain.view;

import java.util.List;

import com.bangpot.meeting.domain.MeetingStatus;

public record MyCreatedMeetingsView(
	List<MyCreatedMeetingsView.Item> items,
	MyCreatedMeetingsView.Page page
) {
	public static MyCreatedMeetingsView of(List<MyCreatedMeetingsView.Item> items, MyCreatedMeetingsView.Page page) {
		return new MyCreatedMeetingsView(items, page);
	}

	public record Item(
		Long meetingId,
		String title,
		MeetingStatus status,
		String date,
		String time,
		Long crewId,
		String crewName
	) {
		public static Item of(
			Long meetingId,
			String title,
			MeetingStatus status,
			String date,
			String time,
			Long crewId,
			String crewName
		) {
			return new Item(meetingId, title, status, date, time, crewId, crewName);
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
