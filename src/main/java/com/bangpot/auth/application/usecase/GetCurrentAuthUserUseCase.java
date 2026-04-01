package com.bangpot.auth.application.usecase;

import java.time.Instant;

/**
 * 현재 인증된 사용자의 인증 상태와 가입 완료 필요 여부를 조회하는 유스케이스다.
 */
public interface GetCurrentAuthUserUseCase {

	/**
	 * 세션 사용자 식별자를 기준으로 guest, temp, full 상태를 조회한다.
	 */
	View handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	enum AuthStatus {
		GUEST,
		TEMP,
		FULL
	}

	record AuthenticatedUserView(Long id, String nickname) {

		public static AuthenticatedUserView of(Long id, String nickname) {
			return new AuthenticatedUserView(id, nickname);
		}
	}

	record View(
		AuthStatus authStatus,
		boolean completionRequired,
		String redirectTo,
		String requiredTermsVersion,
		AuthenticatedUserView user,
		Instant requiredTermsAcceptedAt
	) {

		public static View guest(String requiredTermsVersion) {
			return new View(AuthStatus.GUEST, false, null, requiredTermsVersion, null, null);
		}

		public static View authenticated(
			AuthStatus authStatus,
			boolean completionRequired,
			String redirectTo,
			String requiredTermsVersion,
			AuthenticatedUserView user,
			Instant requiredTermsAcceptedAt
		) {
			return new View(
				authStatus,
				completionRequired,
				redirectTo,
				requiredTermsVersion,
				user,
				requiredTermsAcceptedAt
			);
		}
	}
}
