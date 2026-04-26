package com.bangpot.crew.domain.view;

import java.util.List;

public record CrewJoinRequestsView(
	List<CrewJoinRequestsView.Item> items,
	CrewJoinRequestsView.Page page
) {

	public static CrewJoinRequestsView of(List<CrewJoinRequestsView.Item> items, CrewJoinRequestsView.Page page) {
		return new CrewJoinRequestsView(items, page);
	}

	public record Item(
		Long requestId,
		Long userId,
		String nickname,
		String message,
		String status
	) {
		public static Item of(Long requestId, Long userId, String nickname, String message, String status) {
			return new Item(requestId, userId, nickname, message, status);
		}
	}

	public record Page(int page, int size, boolean hasNext) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
