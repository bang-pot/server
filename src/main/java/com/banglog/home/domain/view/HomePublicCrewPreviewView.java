package com.banglog.home.domain.view;

import java.util.List;

public record HomePublicCrewPreviewView(
	List<HomePublicCrewPreviewView.Item> items
) {
	public static HomePublicCrewPreviewView of(List<HomePublicCrewPreviewView.Item> items) {
		return new HomePublicCrewPreviewView(items);
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
