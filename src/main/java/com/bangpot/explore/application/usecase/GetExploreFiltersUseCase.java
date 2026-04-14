package com.bangpot.explore.application.usecase;

import java.util.List;

public interface GetExploreFiltersUseCase {

	Result handle();

	record RegionOption(
		String name,
		List<String> districts
	) {
		public static RegionOption of(String name, List<String> districts) {
			return new RegionOption(name, districts);
		}
	}

	record Result(
		List<String> genres,
		List<RegionOption> regions
	) {
		public static Result of(List<String> genres, List<RegionOption> regions) {
			return new Result(genres, regions);
		}
	}
}
