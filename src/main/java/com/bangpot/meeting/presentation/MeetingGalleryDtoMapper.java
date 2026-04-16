package com.bangpot.meeting.presentation;

import java.util.List;

import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;

final class MeetingGalleryDtoMapper {

	private MeetingGalleryDtoMapper() {
	}

	static MeetingGalleryDto.MeetingGalleryResponse toResponse(GetCrewMeetingGalleryUseCase.Result result) {
		List<MeetingGalleryDto.MeetingGalleryItemResponse> items = result.items().stream()
			.map(item -> new MeetingGalleryDto.MeetingGalleryItemResponse(
				item.meetingId(),
				item.meetingDate(),
				item.meetingTitle(),
				item.coverPhotoUrl(),
				item.extraPhotoCount()
			))
			.toList();

		return new MeetingGalleryDto.MeetingGalleryResponse(
			items,
			new MeetingGalleryDto.MeetingGalleryPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}
}
