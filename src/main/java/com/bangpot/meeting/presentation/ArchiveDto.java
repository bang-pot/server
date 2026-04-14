package com.bangpot.meeting.presentation;

import java.util.List;

final class ArchiveDto {

	private ArchiveDto() {
	}

	record ArchiveMeetingResponse(
		Long meetingId,
		Long crewId,
		String crewName,
		String themeName,
		String place,
		String date,
		String result,
		String posterImageUrl
	) {
	}

	record ArchiveMeetingsResponse(
		List<ArchiveMeetingResponse> items,
		ArchivePageInfo pageInfo
	) {
	}

	record ArchivePageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}
}

