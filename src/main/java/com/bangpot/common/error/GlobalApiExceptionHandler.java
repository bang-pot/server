package com.bangpot.common.error;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.bangpot.auth.application.exception.AuthCompletionNotAllowedException;
import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.exception.DuplicateNicknameException;
import com.bangpot.auth.application.exception.InvalidNicknameException;
import com.bangpot.auth.application.exception.MissingRequiredTermsAgreementException;
import com.bangpot.auth.error.AuthErrorCode;
import com.bangpot.auth.presentation.UnauthenticatedException;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalApiExceptionHandler extends ResponseEntityExceptionHandler {

	private final ApiErrorResponseFactory apiErrorResponseFactory;

	@ExceptionHandler(UnauthenticatedException.class)
	ResponseEntity<ApiErrorResponse> handleUnauthenticated(UnauthenticatedException exception) {
		return error(AuthErrorCode.AUTH_UNAUTHENTICATED);
	}

	@ExceptionHandler(InvalidNicknameException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidNickname(InvalidNicknameException exception) {
		return error(AuthErrorCode.AUTH_INVALID_NICKNAME);
	}

	@ExceptionHandler(MissingRequiredTermsAgreementException.class)
	ResponseEntity<ApiErrorResponse> handleMissingRequiredTerms(MissingRequiredTermsAgreementException exception) {
		return error(AuthErrorCode.AUTH_REQUIRED_TERMS_AGREEMENT);
	}

	@ExceptionHandler(DuplicateNicknameException.class)
	ResponseEntity<ApiErrorResponse> handleDuplicateNickname(DuplicateNicknameException exception) {
		return error(AuthErrorCode.AUTH_DUPLICATE_NICKNAME);
	}

	@ExceptionHandler(AuthUserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleAuthUserNotFound(AuthUserNotFoundException exception) {
		return error(AuthErrorCode.AUTH_USER_NOT_FOUND);
	}

	@ExceptionHandler(AuthCompletionNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleAuthCompletionNotAllowed(AuthCompletionNotAllowedException exception) {
		return error(AuthErrorCode.AUTH_COMPLETION_NOT_ALLOWED);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleInternalError(Exception exception) {
		return error(CommonErrorCode.COMMON_INTERNAL_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception,
		HttpHeaders headers,
		HttpStatusCode status,
		WebRequest request
	) {
		List<ApiErrorField> fieldErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(fieldError -> new ApiErrorField(
				fieldError.getField(),
				fieldError.getDefaultMessage() == null ? "입력값이 올바르지 않습니다." : fieldError.getDefaultMessage()
			))
			.toList();

		return ResponseEntity.badRequest().body(
			apiErrorResponseFactory.create(CommonErrorCode.COMMON_VALIDATION_ERROR, fieldErrors)
		);
	}

	private ResponseEntity<ApiErrorResponse> error(ApiErrorCode errorCode) {
		return ResponseEntity.status(errorCode.status())
			.body(apiErrorResponseFactory.create(errorCode));
	}
}
