package com.bangpot.meeting.domain.view;

import java.util.List;

public record CrewMeetingGalleryDetailView(
	Long meetingId,
	String meetingDate,
	String meetingTitle,
	List<CrewMeetingGalleryDetailView.Photo> photos,
	int totalPhotoCount
) {

	public static CrewMeetingGalleryDetailView of(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		List<CrewMeetingGalleryDetailView.Photo> photos,
		int totalPhotoCount
	) {
		return new CrewMeetingGalleryDetailView(meetingId, meetingDate, meetingTitle, photos, totalPhotoCount);
	}

	public record Photo(
		Long photoId,
		String url,
		int order
	) {

		public static Photo of(Long photoId, String url, int order) {
			return new Photo(photoId, url, order);
		}
	}

	public record PhotoSource(
		Long photoId,
		String url
	) {
	}
}
