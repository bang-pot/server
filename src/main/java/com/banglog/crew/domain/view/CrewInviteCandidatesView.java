package com.banglog.crew.domain.view;

import java.util.List;

public record CrewInviteCandidatesView(
	List<CrewInviteCandidatesView.Item> items,
	CrewInviteCandidatesView.Page page
) {

	public static CrewInviteCandidatesView of(List<CrewInviteCandidatesView.Item> items, CrewInviteCandidatesView.Page page) {
		return new CrewInviteCandidatesView(items, page);
	}

	public record Item(Long userId, String nickname) {
		public static Item of(Long userId, String nickname) {
			return new Item(userId, nickname);
		}
	}

	public record Page(int page, int size, boolean hasNext) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
