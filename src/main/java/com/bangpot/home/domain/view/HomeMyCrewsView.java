package com.bangpot.home.domain.view;

import java.util.List;

public record HomeMyCrewsView(
	List<HomeMyCrewsView.Item> items,
	Long totalCount
) {
	public static HomeMyCrewsView of(List<HomeMyCrewsView.Item> items, Long totalCount) {
		return new HomeMyCrewsView(items, totalCount);
	}

	public record Item(
		Long crewId,
		String crewName
	) {
		public static Item of(Long crewId, String crewName) {
			return new Item(crewId, crewName);
		}
	}
}
