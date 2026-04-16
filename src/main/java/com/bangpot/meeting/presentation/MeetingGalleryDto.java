package com.bangpot.meeting.presentation;

import java.util.List;

final class MeetingGalleryDto {

	private MeetingGalleryDto() {
	}

	record MeetingGalleryItemResponse(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		String coverPhotoUrl,
		Long extraPhotoCount
	) {
	}

	record MeetingGalleryPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record MeetingGalleryResponse(
		List<MeetingGalleryItemResponse> items,
		MeetingGalleryPageInfo pageInfo
	) {
	}

	record MeetingGalleryDetailPhotoResponse(
		Long photoId,
		String url,
		int order
	) {
	}

	record MeetingGalleryDetailResponse(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		List<MeetingGalleryDetailPhotoResponse> photos,
		int totalPhotoCount
	) {
	}
}
