package com.bangpot.gallerylog.application.usecase;

import java.util.List;

public interface CreateMeetingLogUseCase {

	Result handle(Command command);

	record Command(
		Long meetingId,
		Long userId,
		String body,
		List<PhotoInput> photos
	) {
		public static Command of(Long meetingId, Long userId, String body, List<PhotoInput> photos) {
			return new Command(meetingId, userId, body, photos);
		}
	}

	record PhotoInput(
		String url,
		Long sizeBytes
	) {
		public static PhotoInput of(String url, Long sizeBytes) {
			return new PhotoInput(url, sizeBytes);
		}
	}

	record Result(
		Long logId,
		Long meetingId
	) {
		public static Result of(Long logId, Long meetingId) {
			return new Result(logId, meetingId);
		}
	}
}
