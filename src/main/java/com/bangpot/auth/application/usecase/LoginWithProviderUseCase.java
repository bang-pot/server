package com.bangpot.auth.application.usecase;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUserStatus;

/**
 * OAuth 제공자에서 확인된 식별자로 계정을 찾거나 생성하고 다음 이동 경로를 결정하는 유스케이스다.
 */
public interface LoginWithProviderUseCase {

	/**
	 * 제공자 식별자를 기준으로 기존 사용자를 재로그인시키거나 신규 TEMP 사용자를 만든다.
	 */
	Result handle(Command command);

	record Command(AuthProvider provider, String providerId, String redirectTo) {

		public static Command of(AuthProvider provider, String providerId, String redirectTo) {
			return new Command(provider, providerId, redirectTo);
		}
	}

	record Result(
		Long userId,
		AuthUserStatus authStatus,
		boolean completionRequired,
		String nextPath,
		String pendingRedirectPath
	) {

		public static Result temp(Long userId, String nextPath, String pendingRedirectPath) {
			return new Result(userId, AuthUserStatus.TEMP, true, nextPath, pendingRedirectPath);
		}

		public static Result full(Long userId, String nextPath) {
			return new Result(userId, AuthUserStatus.FULL, false, nextPath, null);
		}
	}
}
