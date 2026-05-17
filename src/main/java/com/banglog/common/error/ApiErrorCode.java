package com.banglog.common.error;

import org.springframework.http.HttpStatus;

public interface ApiErrorCode {

	String code();

	String message();

	HttpStatus status();
}
