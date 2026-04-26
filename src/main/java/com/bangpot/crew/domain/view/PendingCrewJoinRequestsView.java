package com.bangpot.crew.domain.view;

import java.util.List;

public record PendingCrewJoinRequestsView(
	List<PendingCrewJoinRequestsView.Item> items,
	PendingCrewJoinRequestsView.Page page
) {

	public static PendingCrewJoinRequestsView of(
		List<PendingCrewJoinRequestsView.Item> items,
		PendingCrewJoinRequestsView.Page page
	) {
		return new PendingCrewJoinRequestsView(items, page);
	}

	public record Item(Long requestId, Long userId, String nickname) {
		public static Item of(Long requestId, Long userId, String nickname) {
			return new Item(requestId, userId, nickname);
		}
	}

	public record Page(int page, int size, boolean hasNext) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
