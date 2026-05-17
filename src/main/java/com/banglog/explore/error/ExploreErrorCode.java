package com.banglog.explore.error;

import org.springframework.http.HttpStatus;

import com.banglog.common.error.ApiErrorCode;

public enum ExploreErrorCode implements ApiErrorCode {

	EXPLORE_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;

	ExploreErrorCode(HttpStatus status, String message) {
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
