package com.banglog.crew.domain.view;

import java.util.List;

public record MeetingCreateCrewsView(
	List<MeetingCreateCrewsView.Item> items
) {
	public static MeetingCreateCrewsView of(List<MeetingCreateCrewsView.Item> items) {
		return new MeetingCreateCrewsView(items);
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
