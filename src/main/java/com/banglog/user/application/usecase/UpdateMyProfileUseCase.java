package com.banglog.user.application.usecase;

public interface UpdateMyProfileUseCase {

	void handle(Command command);

	record Command(Long userId, String nickname, Long profileImageUploadId) {

		public static Command of(Long userId, String nickname) {
			return new Command(userId, nickname, null);
		}

		public static Command of(Long userId, String nickname, Long profileImageUploadId) {
			return new Command(userId, nickname, profileImageUploadId);
		}
	}
}
