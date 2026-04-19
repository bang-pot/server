package com.bangpot.user.application.exception;

import java.util.List;

import com.bangpot.common.error.ApiErrorField;

public class UserWithdrawalRequestValidationException extends RuntimeException {

	private final List<ApiErrorField> fieldErrors;

	public UserWithdrawalRequestValidationException(List<ApiErrorField> fieldErrors) {
		super("user withdrawal request validation failed");
		this.fieldErrors = fieldErrors;
	}

	public List<ApiErrorField> getFieldErrors() {
		return fieldErrors;
	}
}
