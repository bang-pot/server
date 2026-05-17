package com.banglog.meeting.application.usecase;

public interface CreateMeetingUseCase {

	Result handle(Command command);

	record Command(
		Long crewId,
		Long userId,
		String title,
		String date,
		String time,
		String place,
		String themeName,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description
	) {
		public static Command of(
			Long crewId,
			Long userId,
			String title,
			String date,
			String time,
			String place,
			String themeName,
			Integer capacity,
			Integer totalCost,
			String contactLink,
			String description
		) {
			return new Command(
				crewId,
				userId,
				title,
				date,
				time,
				place,
				themeName,
				capacity,
				totalCost,
				contactLink,
				description
			);
		}
	}

	record Result(
		Long meetingId,
		Long crewId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		String status,
		String result
	) {
		public static Result of(
			Long meetingId,
			Long crewId,
			String title,
			String themeName,
			String place,
			String date,
			String time,
			String status,
			String result
		) {
			return new Result(meetingId, crewId, title, themeName, place, date, time, status, result);
		}
	}
}
