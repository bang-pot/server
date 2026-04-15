package com.bangpot.meeting.presentation;

import java.util.List;

public final class MeetingHistoryDto {

	private MeetingHistoryDto() {
	}

	public record MeetingHistoryListResponse(
		List<MeetingHistoryItemResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	public record MeetingHistoryItemResponse(
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String result,
		String myLogStatus,
		Long logId
	) {
	}

	public record PageInfoResponse(
		int page,
		int size,
		boolean hasNext
	) {
	}
}
