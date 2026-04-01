package com.bangpot.auth.application.usecase;

/**
 * 가입 완료 전에 입력한 닉네임이 현재 사용 가능한지 확인하는 유스케이스다.
 */
public interface CheckNicknameAvailabilityUseCase {

	/**
	 * 닉네임 입력값을 기준으로 중복 여부를 조회한다.
	 */
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
