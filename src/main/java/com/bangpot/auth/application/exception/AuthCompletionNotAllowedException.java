package com.bangpot.auth.application.exception;

public class AuthCompletionNotAllowedException extends RuntimeException {

	public AuthCompletionNotAllowedException(Long userId) {
		super("이미 가입 완료된 사용자입니다: " + userId);
	}
}
