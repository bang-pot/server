package com.banglog.auth.application.usecase;

import com.banglog.auth.domain.AuthUserStatus;

/**
 * 임시 사용자 상태인 계정이 가입 완료 정보를 제출할 때 사용하는 유스케이스다.
 */
public interface CompleteTempUserUseCase {

	/**
	 * 닉네임과 필수 약관 동의 여부를 반영해 TEMP 사용자를 FULL 사용자로 전환한다.
	 */
	Result handle(Command command);

	record Command(Long userId, String nickname, boolean agreedToRequiredTerms) {

		public static Command of(Long userId, String nickname, boolean agreedToRequiredTerms) {
			return new Command(userId, nickname, agreedToRequiredTerms);
		}
	}

	record Result(
		Long userId,
		AuthUserStatus authStatus,
		boolean completionRequired,
		String nextPath
	) {

		public static Result completed(Long userId, String nextPath) {
			return new Result(userId, AuthUserStatus.FULL, false, nextPath);
		}
	}
}
