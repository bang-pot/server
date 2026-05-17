package com.banglog.crew.domain.view;

import java.util.List;

import com.banglog.crew.domain.CrewVisibility;

public record MyCrewsView(
	List<MyCrewsView.Item> items,
	MyCrewsView.Page page
) {
	public static MyCrewsView of(List<MyCrewsView.Item> items, MyCrewsView.Page page) {
		return new MyCrewsView(items, page);
	}

	public record Item(
		Long crewId,
		String crewName,
		CrewVisibility visibility,
		String leaderNickname,
		String coverImageUrl
	) {
		public static Item of(
			Long crewId,
			String crewName,
			CrewVisibility visibility,
			String leaderNickname,
			String coverImageUrl
		) {
			return new Item(crewId, crewName, visibility, leaderNickname, coverImageUrl);
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
