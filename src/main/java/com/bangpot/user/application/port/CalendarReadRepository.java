package com.bangpot.user.application.port;

import java.util.List;

public interface CalendarReadRepository {

	View load(Long userId);

	record View(
		List<Item> items,
		int totalCount
	) {
		public static View of(List<Item> items, int totalCount) {
			return new View(items, totalCount);
		}
	}

	record Item(
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
