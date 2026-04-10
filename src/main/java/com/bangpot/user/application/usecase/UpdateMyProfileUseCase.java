package com.bangpot.user.application.usecase;

public interface UpdateMyProfileUseCase {

	Result handle(Command command);

	record Command(Long userId, String nickname) {

		public static Command of(Long userId, String nickname) {
			return new Command(userId, nickname);
		}
	}

	record Result(Long id, String nickname) {

		public static Result of(Long id, String nickname) {
			return new Result(id, nickname);
		}
	}
}
