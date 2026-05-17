package com.banglog.meeting.presentation;

import java.util.List;

import com.banglog.meeting.domain.view.CrewMeetingGalleryDetailView;
import com.banglog.meeting.domain.view.CrewMeetingGalleryView;

final class MeetingGalleryDtoMapper {

	private MeetingGalleryDtoMapper() {
	}

	static MeetingGalleryDto.MeetingGalleryResponse toResponse(CrewMeetingGalleryView view) {
		List<MeetingGalleryDto.MeetingGalleryItemResponse> items = view.items().stream()
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
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
	}

	static MeetingGalleryDto.MeetingGalleryDetailResponse toResponse(CrewMeetingGalleryDetailView view) {
		List<MeetingGalleryDto.MeetingGalleryDetailPhotoResponse> photos = view.photos().stream()
			.map(photo -> new MeetingGalleryDto.MeetingGalleryDetailPhotoResponse(
				photo.photoId(),
				photo.url(),
				photo.order()
			))
			.toList();

		return new MeetingGalleryDto.MeetingGalleryDetailResponse(
			view.meetingId(),
			view.meetingDate(),
			view.meetingTitle(),
			photos,
			view.totalPhotoCount()
		);
	}
}
