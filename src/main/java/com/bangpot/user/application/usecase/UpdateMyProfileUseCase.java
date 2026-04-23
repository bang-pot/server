package com.bangpot.user.application.usecase;

public interface UpdateMyProfileUseCase {

	void handle(Command command);

	record Command(Long userId, String nickname) {

		public static Command of(Long userId, String nickname) {
			return new Command(userId, nickname);
		}
	}
}
