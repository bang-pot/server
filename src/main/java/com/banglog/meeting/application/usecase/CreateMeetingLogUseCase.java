package com.banglog.meeting.application.usecase;

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
		Long uploadId
	) {
		public static PhotoInput of(Long uploadId) {
			return new PhotoInput(uploadId);
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

