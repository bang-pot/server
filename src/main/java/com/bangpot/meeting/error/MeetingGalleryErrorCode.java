package com.bangpot.meeting.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum MeetingGalleryErrorCode implements ApiErrorCode {

	GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "사진첩 상세 대상을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;

	MeetingGalleryErrorCode(HttpStatus status, String message) {
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
