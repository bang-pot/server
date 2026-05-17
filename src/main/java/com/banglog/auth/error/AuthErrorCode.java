package com.banglog.auth.error;

import org.springframework.http.HttpStatus;

import com.banglog.common.error.ApiErrorCode;

public enum AuthErrorCode implements ApiErrorCode {

	AUTH_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	AUTH_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "유효하지 않은 닉네임입니다."),
	AUTH_REQUIRED_TERMS_AGREEMENT(HttpStatus.BAD_REQUEST, "필수 약관 동의가 필요합니다."),
	AUTH_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	AUTH_USER_NOT_FOUND(HttpStatus.CONFLICT, "사용자를 찾을 수 없습니다."),
	AUTH_COMPLETION_NOT_ALLOWED(HttpStatus.CONFLICT, "프로필 완료를 진행할 수 없습니다."),
	AUTH_WITHDRAWAL_NOT_ALLOWED(HttpStatus.CONFLICT, "지금은 회원탈퇴를 진행할 수 없습니다.");

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
