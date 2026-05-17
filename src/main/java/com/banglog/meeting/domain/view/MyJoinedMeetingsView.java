package com.banglog.meeting.domain.view;

import java.util.List;

import com.banglog.meeting.domain.MeetingResult;
import com.banglog.meeting.domain.MeetingStatus;

public record MyJoinedMeetingsView(
	List<MyJoinedMeetingsView.Item> items,
	MyJoinedMeetingsView.Page page
) {
	public static MyJoinedMeetingsView of(List<MyJoinedMeetingsView.Item> items, MyJoinedMeetingsView.Page page) {
		return new MyJoinedMeetingsView(items, page);
	}

	public record Item(
		Long meetingId,
		String title,
		String themeName,
		Long crewId,
		String crewName,
		String date,
		String time,
		MeetingStatus status,
		MeetingResult result,
		boolean canWriteReview
	) {
		public static Item of(
			Long meetingId,
			String title,
			String themeName,
			Long crewId,
			String crewName,
			String date,
			String time,
			MeetingStatus status,
			MeetingResult result,
			boolean canWriteReview
		) {
			return new Item(
				meetingId,
				title,
				themeName,
				crewId,
				crewName,
				date,
				time,
				status,
				result,
				canWriteReview
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
