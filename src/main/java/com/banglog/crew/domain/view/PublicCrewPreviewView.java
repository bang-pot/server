package com.banglog.crew.domain.view;

import java.util.List;

public record PublicCrewPreviewView(
	List<PublicCrewPreviewView.Item> items
) {
	public static PublicCrewPreviewView of(List<PublicCrewPreviewView.Item> items) {
		return new PublicCrewPreviewView(items);
	}

	public record Item(
		Long crewId,
		String crewName,
		String coverImageUrl,
		Long memberCount
	) {
		public static Item of(
			Long crewId,
			String crewName,
			String coverImageUrl,
			Long memberCount
		) {
			return new Item(crewId, crewName, coverImageUrl, memberCount);
		}
	}
}
