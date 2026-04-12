package com.bangpot.meeting.application.usecase;

import java.util.List;

public interface GetMeetingsUseCase {

	List<View> handle(Query query);

	record Query(Long crewId, Long userId) {
		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

	record View(
		Long meetingId,
		String themeName,
		String place,
		String date,
		String time,
		String status,
		String result,
		Integer capacity
	) {
		public static View of(
			Long meetingId,
			String themeName,
			String place,
			String date,
			String time,
			String status,
			String result,
			Integer capacity
		) {
			return new View(meetingId, themeName, place, date, time, status, result, capacity);
		}
	}
}
