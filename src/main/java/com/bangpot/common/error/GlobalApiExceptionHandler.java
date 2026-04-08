package com.bangpot.common.error;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import com.bangpot.crew.application.exception.CrewAlreadyJoinedException;
import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.exception.InvalidCrewVisibilityException;
import com.bangpot.crew.error.CrewErrorCode;

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

	@ExceptionHandler(DuplicateCrewNameException.class)
	ResponseEntity<ApiErrorResponse> handleDuplicateCrewName(DuplicateCrewNameException exception) {
		return ResponseEntity.status(CrewErrorCode.CREW_DUPLICATE_NAME.status())
			.body(apiErrorResponseFactory.create(
				CrewErrorCode.CREW_DUPLICATE_NAME,
				List.of(new ApiErrorField("name", CrewErrorCode.CREW_DUPLICATE_NAME.message()))
			));
	}

	@ExceptionHandler(InvalidCrewVisibilityException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidCrewVisibility(InvalidCrewVisibilityException exception) {
		return ResponseEntity.badRequest().body(
			apiErrorResponseFactory.create(
				CommonErrorCode.COMMON_VALIDATION_ERROR,
				List.of(new ApiErrorField("visibility", "공개/비공개 설정값이 올바르지 않습니다."))
			)
		);
	}

	@ExceptionHandler(CrewNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleCrewNotFound(CrewNotFoundException exception) {
		return error(CrewErrorCode.CREW_NOT_FOUND);
	}

	@ExceptionHandler(CrewJoinRequestNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewJoinRequestNotAllowed(CrewJoinRequestNotAllowedException exception) {
		return error(CrewErrorCode.CREW_JOIN_REQUEST_NOT_ALLOWED);
	}

	@ExceptionHandler(CrewAlreadyJoinedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewAlreadyJoined(CrewAlreadyJoinedException exception) {
		return error(CrewErrorCode.CREW_ALREADY_JOINED);
	}

	@ExceptionHandler(CrewJoinRequestAlreadyPendingException.class)
	ResponseEntity<ApiErrorResponse> handleCrewJoinRequestAlreadyPending(
		CrewJoinRequestAlreadyPendingException exception
	) {
		return error(CrewErrorCode.CREW_JOIN_REQUEST_ALREADY_PENDING);
	}

	@ExceptionHandler(AuthUserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleAuthUserNotFound(AuthUserNotFoundException exception) {
		return error(AuthErrorCode.AUTH_USER_NOT_FOUND);
	}

	@ExceptionHandler(AuthCompletionNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleAuthCompletionNotAllowed(AuthCompletionNotAllowedException exception) {
		return error(AuthErrorCode.AUTH_COMPLETION_NOT_ALLOWED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		return error(AuthErrorCode.AUTH_ACCESS_DENIED);
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
