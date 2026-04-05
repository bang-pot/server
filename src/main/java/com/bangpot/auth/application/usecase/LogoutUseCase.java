package com.bangpot.auth.application.usecase;

public interface LogoutUseCase {

	void handle(Command command);

	record Command(Long userId) {

		public static Command of(Long userId) {
			return new Command(userId);
		}
	}
}
