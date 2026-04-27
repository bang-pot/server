package com.bangpot.meeting.domain.view;

import java.util.List;

public record CrewScheduleView(
	List<CrewScheduleView.Item> items
) {

	public static CrewScheduleView of(List<CrewScheduleView.Item> items) {
		return new CrewScheduleView(items);
	}

	public record Item(
		Long meetingId,
		String themeName,
		String date,
		String time,
		String meetingStatus,
		String recruitmentStatus,
		String place,
		Long participantCount,
		boolean isCanceled
	) {

		public static Item of(
			Long meetingId,
			String themeName,
			String date,
			String time,
			String meetingStatus,
			String recruitmentStatus,
			String place,
			Long participantCount,
			boolean isCanceled
		) {
			return new Item(
				meetingId,
				themeName,
				date,
				time,
				meetingStatus,
				recruitmentStatus,
				place,
				participantCount,
				isCanceled
			);
		}
	}
}
