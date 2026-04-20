package com.bangpot.crew.application.exception;

import java.util.List;

import com.bangpot.common.error.ApiErrorField;

public class CrewScheduleRequestValidationException extends RuntimeException {

	private final List<ApiErrorField> fieldErrors;

	public CrewScheduleRequestValidationException(List<ApiErrorField> fieldErrors) {
		super("crew schedule request validation failed");
		this.fieldErrors = fieldErrors;
	}

	public List<ApiErrorField> getFieldErrors() {
		return fieldErrors;
	}
}
