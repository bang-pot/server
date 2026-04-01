package com.bangpot.auth.presentation;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bangpot.auth.application.exception.AuthCompletionNotAllowedException;
import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.exception.DuplicateNicknameException;
import com.bangpot.auth.application.exception.InvalidNicknameException;
import com.bangpot.auth.application.exception.MissingRequiredTermsAgreementException;

@RestControllerAdvice
class AuthControllerAdvice {

	@ExceptionHandler(UnauthenticatedException.class)
	ResponseEntity<Map<String, Object>> handleUnauthenticated(UnauthenticatedException exception) {
		return error(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHENTICATED", exception.getMessage());
	}

	@ExceptionHandler({
		InvalidNicknameException.class,
		MissingRequiredTermsAgreementException.class
	})
	ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException exception) {
		return error(HttpStatus.BAD_REQUEST, "AUTH_BAD_REQUEST", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
		FieldError fieldError = exception.getBindingResult().getFieldError();
		String message = fieldError == null || fieldError.getDefaultMessage() == null
			? "잘못된 요청입니다."
			: fieldError.getDefaultMessage();
		return error(HttpStatus.BAD_REQUEST, "AUTH_BAD_REQUEST", message);
	}

	@ExceptionHandler(DuplicateNicknameException.class)
	ResponseEntity<Map<String, Object>> handleDuplicateNickname(DuplicateNicknameException exception) {
		return error(HttpStatus.CONFLICT, "AUTH_DUPLICATE_NICKNAME", exception.getMessage());
	}

	@ExceptionHandler({
		AuthUserNotFoundException.class,
		AuthCompletionNotAllowedException.class
	})
	ResponseEntity<Map<String, Object>> handleConflict(RuntimeException exception) {
		return error(HttpStatus.CONFLICT, "AUTH_CONFLICT", exception.getMessage());
	}

	private ResponseEntity<Map<String, Object>> error(
		HttpStatus status,
		String code,
		String message
	) {
		return ResponseEntity.status(status).body(Map.of(
			"code", code,
			"message", message,
			"status", status.value()
		));
	}
}
