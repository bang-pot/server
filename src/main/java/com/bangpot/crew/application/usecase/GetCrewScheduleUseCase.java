package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetCrewScheduleUseCase {

	Result handle(Query query);

	record Query(
		Long crewId,
		Long userId,
		String from,
		String to
	) {
		public static Query of(Long crewId, Long userId, String from, String to) {
			return new Query(crewId, userId, from, to);
		}
	}

	record Result(
		List<Item> items
	) {
		public static Result of(List<Item> items) {
			return new Result(items);
		}
	}

	record Item(
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
