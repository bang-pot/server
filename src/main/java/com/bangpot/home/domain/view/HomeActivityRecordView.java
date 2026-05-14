package com.bangpot.home.domain.view;

public record HomeActivityRecordView(
	Long completedCount,
	Integer successRate
) {
	public static HomeActivityRecordView of(Long completedCount, Integer successRate) {
		return new HomeActivityRecordView(completedCount, successRate);
	}

	public static HomeActivityRecordView empty() {
		return new HomeActivityRecordView(0L, 0);
	}
}
