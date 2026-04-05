package com.bangpot.common.error;

import java.util.List;

public record ApiErrorResponse(
	String code,
	String message,
	String requestId,
	List<ApiErrorField> fieldErrors
) {
}
