package com.bangpot.crew.domain.view;

import java.util.List;

public record PublicCrewCardsView(
	List<PublicCrewCardsView.Item> items,
	PublicCrewCardsView.Page page
) {

	public static PublicCrewCardsView of(List<PublicCrewCardsView.Item> items, PublicCrewCardsView.Page page) {
		return new PublicCrewCardsView(items, page);
	}

	public record Item(
		Long crewId,
		String name,
		String description,
		String imageUrl
	) {

		public static Item of(Long crewId, String name, String description, String imageUrl) {
			return new Item(crewId, name, description, imageUrl);
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
