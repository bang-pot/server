package com.bangpot.user.application.port;

import java.util.List;

public interface PendingCrewReadRepository {

	SearchResult search(Long userId, int page, int size);

	CancelResult cancel(Long userId, Long joinRequestId);

	record SearchResult(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static SearchResult of(List<Item> items, PageInfo pageInfo) {
			return new SearchResult(items, pageInfo);
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

	record CancelResult(
		Long joinRequestId,
		Long crewId
	) {
		public static CancelResult of(Long joinRequestId, Long crewId) {
			return new CancelResult(joinRequestId, crewId);
		}
	}
}
