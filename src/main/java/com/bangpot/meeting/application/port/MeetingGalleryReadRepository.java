package com.bangpot.meeting.application.port;

import java.util.List;
import java.util.Optional;

public interface MeetingGalleryReadRepository {

	Optional<Detail> findDetail(Long crewId, Long meetingId);

	record Detail(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		List<DetailPhoto> photos,
		int totalPhotoCount
	) {
		public static Detail of(
			Long meetingId,
			String meetingDate,
			String meetingTitle,
			List<DetailPhoto> photos,
			int totalPhotoCount
		) {
			return new Detail(meetingId, meetingDate, meetingTitle, photos, totalPhotoCount);
		}
	}

	record DetailPhoto(
		Long photoId,
		String url,
		int order
	) {
		public static DetailPhoto of(Long photoId, String url, int order) {
			return new DetailPhoto(photoId, url, order);
		}
	}
}
