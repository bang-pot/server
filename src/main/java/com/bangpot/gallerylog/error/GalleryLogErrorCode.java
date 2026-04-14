package com.bangpot.gallerylog.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum GalleryLogErrorCode implements ApiErrorCode {

	LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "방탈로그를 찾을 수 없습니다."),
	LOG_WRITE_NOT_ALLOWED(HttpStatus.CONFLICT, "지금은 방탈로그를 작성할 수 없습니다."),
	LOG_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 이 모임에 방탈로그를 작성했습니다.");

	private final HttpStatus status;
	private final String message;

	GalleryLogErrorCode(HttpStatus status, String message) {
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
