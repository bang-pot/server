package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyPendingCrewsUseCase {

	Result handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
		}
	}

	record Result(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static Result of(List<Item> items, PageInfo pageInfo) {
			return new Result(items, pageInfo);
		}
	}

	record Item(
		Long joinRequestId,
		Long crewId,
		String crewName,
		String requestedAt,
		String messageSummary
	) {
		public static Item of(
			Long joinRequestId,
			Long crewId,
			String crewName,
			String requestedAt,
			String messageSummary
		) {
			return new Item(joinRequestId, crewId, crewName, requestedAt, messageSummary);
		}
	}

	record PageInfo(
		int page,
		int size,
		boolean hasNext
	) {
		public static PageInfo of(int page, int size, boolean hasNext) {
			return new PageInfo(page, size, hasNext);
		}
	}
}
