package com.bangpot.image.application.usecase;

import java.util.List;

import com.bangpot.image.domain.ImageUploadCategory;

public interface AttachImageUploadUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		ImageUploadCategory category,
		String finalDirectory,
		List<Long> uploadIds
	) {
		public static Command of(
			Long userId,
			ImageUploadCategory category,
			String finalDirectory,
			List<Long> uploadIds
		) {
			return new Command(userId, category, finalDirectory, uploadIds);
		}
	}

	record Result(List<String> urls) {
		public static Result of(List<String> urls) {
			return new Result(urls);
		}
	}
}
