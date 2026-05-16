package com.bangpot.meeting.application.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface UploadMeetingLogPhotoUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		MultipartFile file
	) {
		public static Command of(Long userId, MultipartFile file) {
			return new Command(userId, file);
		}
	}

	record Result(
		String url,
		Long sizeBytes
	) {
		public static Result of(String url, Long sizeBytes) {
			return new Result(url, sizeBytes);
		}
	}
}

