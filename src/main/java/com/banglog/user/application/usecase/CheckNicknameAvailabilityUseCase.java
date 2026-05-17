package com.banglog.user.application.usecase;

public interface CheckNicknameAvailabilityUseCase {

	Result handle(Query query);

	record Query(String nickname) {

		public static Query of(String nickname) {
			return new Query(nickname);
		}
	}

	record Result(String nickname, boolean available) {

		public static Result invalid() {
			return new Result(null, false);
		}

		public static Result of(String nickname, boolean available) {
			return new Result(nickname, available);
		}
	}
}
