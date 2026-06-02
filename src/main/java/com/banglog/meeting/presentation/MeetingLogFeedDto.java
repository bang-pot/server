package com.banglog.meeting.presentation;

import java.time.Instant;
import java.util.List;

final class MeetingLogFeedDto {

	private MeetingLogFeedDto() {
	}

	record MeetingLogFeedItemResponse(
		Long logId,
		Long meetingId,
		String authorNickname,
		String meetingTitle,
		String themeName,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long extraPhotoCount,
		String result
	) {
	}

	record MeetingLogFeedPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record MeetingLogFeedResponse(
		List<MeetingLogFeedItemResponse> items,
		MeetingLogFeedPageInfo pageInfo
	) {
	}
}
