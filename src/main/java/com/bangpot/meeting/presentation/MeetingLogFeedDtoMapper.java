package com.bangpot.meeting.presentation;

import com.bangpot.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;

final class MeetingLogFeedDtoMapper {

	private MeetingLogFeedDtoMapper() {
	}

	static MeetingLogFeedDto.MeetingLogFeedResponse toResponse(GetCrewMeetingLogFeedUseCase.Result result) {
		return new MeetingLogFeedDto.MeetingLogFeedResponse(
			result.items().stream()
				.map(item -> new MeetingLogFeedDto.MeetingLogFeedItemResponse(
					item.logId(),
					item.meetingId(),
					item.authorNickname(),
					item.meetingTitle(),
					item.meetingDate(),
					item.createdAt(),
					item.excerpt(),
					item.coverPhotoUrl(),
					item.extraPhotoCount()
				))
				.toList(),
			new MeetingLogFeedDto.MeetingLogFeedPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}
}
