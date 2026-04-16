package com.bangpot.meeting.application.usecase;

import java.util.List;

public interface GetCrewMeetingGalleryDetailUseCase {

	Result handle(Query query);

	record Query(
		Long crewId,
		Long meetingId,
		Long userId
	) {
		public static Query of(Long crewId, Long meetingId, Long userId) {
			return new Query(crewId, meetingId, userId);
		}
	}

	record Result(
		Long meetingId,
		String meetingDate,
		String meetingTitle,
		List<Photo> photos,
		int totalPhotoCount
	) {
		public static Result of(
			Long meetingId,
			String meetingDate,
			String meetingTitle,
			List<Photo> photos,
			int totalPhotoCount
		) {
			return new Result(meetingId, meetingDate, meetingTitle, photos, totalPhotoCount);
		}
	}

	record Photo(
		Long photoId,
		String url,
		int order
	) {
		public static Photo of(Long photoId, String url, int order) {
			return new Photo(photoId, url, order);
		}
	}
}
