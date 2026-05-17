package com.bangpot.image.application.exception;

import java.util.List;

import com.bangpot.common.error.ApiErrorField;

public class ImageUploadRequestValidationException extends RuntimeException {

	private final List<ApiErrorField> fieldErrors;

	public ImageUploadRequestValidationException(List<ApiErrorField> fieldErrors) {
		super("image upload request validation failed");
		this.fieldErrors = fieldErrors;
	}

	public List<ApiErrorField> getFieldErrors() {
		return fieldErrors;
	}
}
