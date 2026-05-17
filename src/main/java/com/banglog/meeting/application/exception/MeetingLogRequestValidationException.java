package com.banglog.meeting.application.exception;

import java.util.List;

import com.banglog.common.error.ApiErrorField;

public class MeetingLogRequestValidationException extends RuntimeException {

	private final List<ApiErrorField> fieldErrors;

	public MeetingLogRequestValidationException(List<ApiErrorField> fieldErrors) {
		super("meeting log request validation failed");
		this.fieldErrors = fieldErrors;
	}

	public List<ApiErrorField> getFieldErrors() {
		return fieldErrors;
	}
}
