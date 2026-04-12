package com.bangpot.meeting.application.usecase;

public interface GetMeetingDetailUseCase {

	Result handle(Query query);

	record Query(Long crewId, Long meetingId, Long userId) {
		public static Query of(Long crewId, Long meetingId, Long userId) {
			return new Query(crewId, meetingId, userId);
		}
	}

	record Result(
		Long meetingId,
		Long crewId,
		Long hostUserId,
		String themeName,
		String place,
		String date,
		String time,
		Integer capacity,
		Integer totalCost,
		String reservationLink,
		String openChatLink,
		String description,
		String status,
		String result,
		String myParticipationStatus
	) {
		public static Result of(
			Long meetingId,
			Long crewId,
			Long hostUserId,
			String themeName,
			String place,
			String date,
			String time,
			Integer capacity,
			Integer totalCost,
			String reservationLink,
			String openChatLink,
			String description,
			String status,
			String result,
			String myParticipationStatus
		) {
			return new Result(
				meetingId,
				crewId,
				hostUserId,
				themeName,
				place,
				date,
				time,
				capacity,
				totalCost,
				reservationLink,
				openChatLink,
				description,
				status,
				result,
				myParticipationStatus
			);
		}
	}
}
