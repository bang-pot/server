package com.bangpot.meeting.application.usecase;

public interface CreateMeetingUseCase {

	Result handle(Command command);

	record Command(
		Long crewId,
		Long userId,
		String date,
		String time,
		String place,
		String themeName,
		Integer capacity,
		Integer totalCost,
		String reservationLink,
		String openChatLink,
		String description
	) {
		public static Command of(
			Long crewId,
			Long userId,
			String date,
			String time,
			String place,
			String themeName,
			Integer capacity,
			Integer totalCost,
			String reservationLink,
			String openChatLink,
			String description
		) {
			return new Command(
				crewId,
				userId,
				date,
				time,
				place,
				themeName,
				capacity,
				totalCost,
				reservationLink,
				openChatLink,
				description
			);
		}
	}

	record Result(
		Long meetingId,
		Long crewId,
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
			String themeName,
			String place,
			String date,
			String time,
			String status,
			String result
		) {
			return new Result(meetingId, crewId, themeName, place, date, time, status, result);
		}
	}
}
