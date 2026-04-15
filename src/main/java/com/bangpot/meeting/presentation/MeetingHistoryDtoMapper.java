package com.bangpot.meeting.presentation;

import com.bangpot.meeting.application.usecase.GetCrewMeetingHistoryUseCase;

final class MeetingHistoryDtoMapper {

	private MeetingHistoryDtoMapper() {
	}

	static MeetingHistoryDto.MeetingHistoryListResponse toResponse(GetCrewMeetingHistoryUseCase.Result result) {
		return new MeetingHistoryDto.MeetingHistoryListResponse(
			result.items().stream()
				.map(item -> new MeetingHistoryDto.MeetingHistoryItemResponse(
					item.meetingId(),
					item.meetingTitle(),
					item.themeName(),
					item.place(),
					item.date(),
					item.result(),
					item.myLogStatus(),
					item.logId()
				))
				.toList(),
			new MeetingHistoryDto.PageInfoResponse(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}
}
