package com.bangpot.meeting.presentation;

import java.util.List;

import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
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

	static MeetingGalleryDto.MeetingGalleryDetailResponse toResponse(GetCrewMeetingGalleryDetailUseCase.Result result) {
		List<MeetingGalleryDto.MeetingGalleryDetailPhotoResponse> photos = result.photos().stream()
			.map(photo -> new MeetingGalleryDto.MeetingGalleryDetailPhotoResponse(
				photo.photoId(),
				photo.url(),
				photo.order()
			))
			.toList();

		return new MeetingGalleryDto.MeetingGalleryDetailResponse(
			result.meetingId(),
			result.meetingDate(),
			result.meetingTitle(),
			photos,
			result.totalPhotoCount()
		);
	}
}
