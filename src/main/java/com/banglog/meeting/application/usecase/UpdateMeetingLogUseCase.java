package com.banglog.meeting.application.usecase;

import java.util.List;

public interface UpdateMeetingLogUseCase {

	Result handle(Command command);

	record PhotoInput(
		Long uploadId
	) {
		public static PhotoInput of(Long uploadId) {
			return new PhotoInput(uploadId);
		}
	}

	record Command(
		Long logId,
		Long userId,
		String body,
		List<PhotoInput> photos,
		String result
	) {
		public static Command of(Long logId, Long userId, String body, List<PhotoInput> photos) {
			return new Command(logId, userId, body, photos, null);
		}

		public static Command of(Long logId, Long userId, String body, List<PhotoInput> photos, String result) {
			return new Command(logId, userId, body, photos, result);
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

