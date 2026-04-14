package com.bangpot.meeting.application.usecase;

public interface UpdateMeetingUseCase {

	Result handle(Command command);

	record Command(
		Long crewId,
		Long meetingId,
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
			Long meetingId,
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
				meetingId,
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
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description,
		String status,
		String result
	) {
		public static Result of(
			Long meetingId,
			Long crewId,
			Long hostUserId,
			String title,
			String themeName,
			String place,
			String date,
			String time,
			Integer capacity,
			Integer totalCost,
			String contactLink,
			String description,
			String status,
			String result
		) {
			return new Result(
				meetingId,
				crewId,
				hostUserId,
				title,
				themeName,
				place,
				date,
				time,
				capacity,
				totalCost,
				contactLink,
				description,
				status,
				result
			);
		}
	}
}
