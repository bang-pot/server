package com.banglog.meeting.domain.view;

public record MeetingActivityRecordView(
	Long completedCount,
	Long successCount
) {
	public static MeetingActivityRecordView of(Long completedCount, Long successCount) {
		return new MeetingActivityRecordView(completedCount, successCount);
	}

	public static MeetingActivityRecordView empty() {
		return new MeetingActivityRecordView(0L, 0L);
	}
}
