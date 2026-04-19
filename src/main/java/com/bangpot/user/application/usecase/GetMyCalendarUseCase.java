package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyCalendarUseCase {

	Result handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record Result(
		List<Item> items,
		int totalCount
	) {
		public static Result of(List<Item> items, int totalCount) {
			return new Result(items, totalCount);
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
