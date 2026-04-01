package com.bangpot.auth.application.exception;

public class AuthUserNotFoundException extends RuntimeException {

	public AuthUserNotFoundException(Long userId) {
		super("인증 사용자를 찾을 수 없습니다: " + userId);
	}
}
