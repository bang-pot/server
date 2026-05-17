package com.banglog.crew.domain.view;

import java.util.List;

import com.banglog.crew.domain.CrewVisibility;

public record ExploreCrewCardsView(
	List<ExploreCrewCardsView.Item> items,
	ExploreCrewCardsView.Page page
) {

	public static ExploreCrewCardsView of(List<ExploreCrewCardsView.Item> items, ExploreCrewCardsView.Page page) {
		return new ExploreCrewCardsView(items, page);
	}

	public record Item(
		Long crewId,
		String name,
		String description,
		String imageUrl,
		CrewVisibility visibility,
		String leaderNickname,
		Long memberCount
	) {

		public static Item of(
			Long crewId,
			String name,
			String description,
			String imageUrl,
			CrewVisibility visibility,
			String leaderNickname,
			Long memberCount
		) {
			return new Item(crewId, name, description, imageUrl, visibility, leaderNickname, memberCount);
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
