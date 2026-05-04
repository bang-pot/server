package com.bangpot.crew.domain;

public enum ExploreCrewSort {
	LATEST,
	OLDEST,
	MEMBER_COUNT_DESC,
	MEMBER_COUNT_ASC;

	public static ExploreCrewSort from(String value) {
		if (value == null || value.isBlank()) {
			return LATEST;
		}
		return ExploreCrewSort.valueOf(value.trim());
	}
}
