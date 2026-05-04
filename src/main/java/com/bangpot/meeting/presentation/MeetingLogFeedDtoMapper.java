package com.bangpot.meeting.presentation;

import com.bangpot.meeting.domain.view.CrewMeetingLogFeedView;

final class MeetingLogFeedDtoMapper {

	private MeetingLogFeedDtoMapper() {
	}

	static MeetingLogFeedDto.MeetingLogFeedResponse toResponse(CrewMeetingLogFeedView view) {
		return new MeetingLogFeedDto.MeetingLogFeedResponse(
			view.items().stream()
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
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
	}
}
