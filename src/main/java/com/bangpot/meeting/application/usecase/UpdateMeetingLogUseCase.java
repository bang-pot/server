package com.bangpot.meeting.application.usecase;

import java.util.List;

public interface UpdateMeetingLogUseCase {

	Result handle(Command command);

	record PhotoInput(
		String url,
		Long sizeBytes
	) {
		public static PhotoInput of(String url, Long sizeBytes) {
			return new PhotoInput(url, sizeBytes);
		}
	}

	record Command(
		Long logId,
		Long userId,
		String body,
		List<PhotoInput> photos
	) {
		public static Command of(Long logId, Long userId, String body, List<PhotoInput> photos) {
			return new Command(logId, userId, body, photos);
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

