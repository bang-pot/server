package com.bangpot.image.application.usecase;

import org.springframework.web.multipart.MultipartFile;

import com.bangpot.image.domain.ImageUploadCategory;

public interface UploadTemporaryImageUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		ImageUploadCategory category,
		MultipartFile file
	) {
		public static Command of(
			Long userId,
			ImageUploadCategory category,
			MultipartFile file
		) {
			return new Command(userId, category, file);
		}
	}

	record Result(
		Long uploadId,
		String url,
		Long sizeBytes
	) {
		public static Result of(Long uploadId, String url, Long sizeBytes) {
			return new Result(uploadId, url, sizeBytes);
		}
	}
}
