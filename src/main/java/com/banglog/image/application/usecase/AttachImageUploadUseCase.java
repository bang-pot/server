package com.banglog.image.application.usecase;

import java.util.List;

import com.banglog.image.domain.ImageUploadCategory;

public interface AttachImageUploadUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		ImageUploadCategory category,
		List<Long> uploadIds
	) {
		public static Command of(
			Long userId,
			ImageUploadCategory category,
			List<Long> uploadIds
		) {
			return new Command(userId, category, uploadIds);
		}
	}

	record Result(List<String> urls) {
		public static Result of(List<String> urls) {
			return new Result(urls);
		}
	}
}
