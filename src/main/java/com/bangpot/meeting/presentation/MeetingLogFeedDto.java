package com.bangpot.meeting.presentation;

import java.time.Instant;
import java.util.List;

final class MeetingLogFeedDto {

	private MeetingLogFeedDto() {
	}

	record MeetingLogFeedItemResponse(
		Long logId,
		Long meetingId,
		Long crewId,
		String authorNickname,
		String meetingTitle,
		String themeName,
		String date,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long photoCount
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
