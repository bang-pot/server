package com.banglog.crew.domain.view;

import java.util.List;

public record MyPendingCrewsView(
	List<MyPendingCrewsView.Item> items,
	MyPendingCrewsView.Page page
) {
	public static MyPendingCrewsView of(List<MyPendingCrewsView.Item> items, MyPendingCrewsView.Page page) {
		return new MyPendingCrewsView(items, page);
	}

	public record Item(
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

	public record Page(
		int page,
		int size,
		boolean hasNext
	) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
