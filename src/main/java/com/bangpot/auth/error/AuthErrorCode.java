package com.bangpot.auth.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum AuthErrorCode implements ApiErrorCode {

	AUTH_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	AUTH_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임이 올바르지 않습니다."),
	AUTH_REQUIRED_TERMS_AGREEMENT(HttpStatus.BAD_REQUEST, "필수 약관 동의가 필요합니다."),
	AUTH_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	AUTH_USER_NOT_FOUND(HttpStatus.CONFLICT, "인증 사용자를 찾을 수 없습니다."),
	AUTH_COMPLETION_NOT_ALLOWED(HttpStatus.CONFLICT, "이미 가입 완료된 사용자입니다.");

	private final HttpStatus status;
	private final String message;

	AuthErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public HttpStatus status() {
		return status;
	}
}
