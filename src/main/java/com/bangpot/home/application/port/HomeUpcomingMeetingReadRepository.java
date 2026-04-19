package com.bangpot.home.application.port;

import java.util.List;

public interface HomeUpcomingMeetingReadRepository {

	Result findUpcomingMeetings(Long userId, int limit, String currentDate, String currentTime);

	record Result(
		List<Item> items,
		Long totalCount
	) {
		public static Result of(List<Item> items, Long totalCount) {
			return new Result(items, totalCount);
		}
	}

	record Item(
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
