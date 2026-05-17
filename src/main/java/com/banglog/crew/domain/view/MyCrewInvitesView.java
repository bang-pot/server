package com.banglog.crew.domain.view;

import java.util.List;

import com.banglog.crew.domain.CrewInviteStatus;

public record MyCrewInvitesView(
	List<MyCrewInvitesView.Item> items,
	MyCrewInvitesView.Page page
) {

	public static MyCrewInvitesView of(List<MyCrewInvitesView.Item> items, MyCrewInvitesView.Page page) {
		return new MyCrewInvitesView(items, page);
	}

	public record Item(
		Long inviteId,
		Long crewId,
		String crewName,
		String inviterNickname,
		CrewInviteStatus status
	) {

		public static Item of(
			Long inviteId,
			Long crewId,
			String crewName,
			String inviterNickname,
			CrewInviteStatus status
		) {
			return new Item(inviteId, crewId, crewName, inviterNickname, status);
		}
	}

	public record Page(int page, int size, boolean hasNext) {
		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
	}
}
