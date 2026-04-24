package com.bangpot.explore.domain.view;

import java.util.List;

public record ExploreFiltersView(
	List<String> genres,
	List<ExploreFiltersView.Region> regions
) {
	public static ExploreFiltersView of(List<String> genres, List<ExploreFiltersView.Region> regions) {
		return new ExploreFiltersView(genres, regions);
	}

	public record Region(
		String name,
		List<String> districts
	) {
		public static Region of(String name, List<String> districts) {
			return new Region(name, districts);
		}
	}
}
