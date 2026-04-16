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
import com.bangpot.auth.application.exception.MissingRequiredTermsAgreementException;
import com.bangpot.auth.error.AuthErrorCode;
import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.crew.application.exception.CrewAlreadyJoinedException;
import com.bangpot.crew.application.exception.CrewDeleteNameMismatchException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMeetingsException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMembersException;
import com.bangpot.crew.application.exception.CrewInviteAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewLeaderLeaveNotAllowedException;
import com.bangpot.crew.application.exception.CrewLeaveNotAllowedForHostedMeetingException;
import com.bangpot.crew.application.exception.CrewInviteNotFoundException;
import com.bangpot.crew.application.exception.CrewInviteNotAllowedException;
import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.bangpot.crew.application.exception.CrewRemoveMemberTargetNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.exception.CrewTransferLeadershipTargetNotAllowedException;
import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.exception.InvalidCrewVisibilityException;
import com.bangpot.crew.error.CrewErrorCode;
import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.error.ExploreErrorCode;
import com.bangpot.meeting.application.exception.MeetingLogAlreadyExistsException;
import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.exception.MeetingHostCannotCancelParticipationException;
import com.bangpot.meeting.application.exception.MeetingEditNotAllowedException;
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.exception.MeetingInvalidStatusTransitionException;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyJoinedException;
import com.bangpot.meeting.application.exception.MeetingParticipationNotJoinedException;
import com.bangpot.meeting.application.exception.MeetingResultAlreadyRecordedException;
import com.bangpot.meeting.application.exception.MeetingResultRecordNotAllowedException;
import com.bangpot.meeting.error.MeetingErrorCode;
import com.bangpot.meeting.error.MeetingGalleryErrorCode;
import com.bangpot.meeting.error.MeetingLogErrorCode;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.exception.UserNotFoundException;

import jakarta.validation.ConstraintViolationException;
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

	@ExceptionHandler(CrewInviteNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewInviteNotAllowed(CrewInviteNotAllowedException exception) {
		return error(CrewErrorCode.CREW_INVITE_NOT_ALLOWED);
	}

	@ExceptionHandler(CrewInviteAlreadyPendingException.class)
	ResponseEntity<ApiErrorResponse> handleCrewInviteAlreadyPending(CrewInviteAlreadyPendingException exception) {
		return error(CrewErrorCode.CREW_INVITE_ALREADY_PENDING);
	}

	@ExceptionHandler(CrewInviteNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleCrewInviteNotFound(CrewInviteNotFoundException exception) {
		return error(CrewErrorCode.CREW_INVITE_NOT_FOUND);
	}

	@ExceptionHandler(CrewLeaderLeaveNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewLeaderLeaveNotAllowed(CrewLeaderLeaveNotAllowedException exception) {
		return error(CrewErrorCode.CREW_LEADER_LEAVE_NOT_ALLOWED);
	}

	@ExceptionHandler(CrewLeaveNotAllowedForHostedMeetingException.class)
	ResponseEntity<ApiErrorResponse> handleCrewLeaveNotAllowedForHostedMeeting(
		CrewLeaveNotAllowedForHostedMeetingException exception
	) {
		return error(CrewErrorCode.CREW_LEAVE_NOT_ALLOWED_FOR_HOSTED_MEETING);
	}

	@ExceptionHandler(CrewTransferLeadershipTargetNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewTransferLeadershipTargetNotAllowed(
		CrewTransferLeadershipTargetNotAllowedException exception
	) {
		return error(CrewErrorCode.CREW_TRANSFER_LEADERSHIP_TARGET_NOT_ALLOWED);
	}

	@ExceptionHandler(CrewRemoveMemberTargetNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleCrewRemoveMemberTargetNotAllowed(
		CrewRemoveMemberTargetNotAllowedException exception
	) {
		return error(CrewErrorCode.CREW_MEMBER_REMOVE_TARGET_NOT_ALLOWED);
	}

	@ExceptionHandler(CrewDeleteNotAllowedWithActiveMembersException.class)
	ResponseEntity<ApiErrorResponse> handleCrewDeleteNotAllowedWithActiveMembers(
		CrewDeleteNotAllowedWithActiveMembersException exception
	) {
		return error(CrewErrorCode.CREW_DELETE_NOT_ALLOWED_WITH_ACTIVE_MEMBERS);
	}

	@ExceptionHandler(CrewDeleteNotAllowedWithActiveMeetingsException.class)
	ResponseEntity<ApiErrorResponse> handleCrewDeleteNotAllowedWithActiveMeetings(
		CrewDeleteNotAllowedWithActiveMeetingsException exception
	) {
		return error(CrewErrorCode.CREW_DELETE_NOT_ALLOWED_WITH_ACTIVE_MEETINGS);
	}

	@ExceptionHandler(CrewDeleteNameMismatchException.class)
	ResponseEntity<ApiErrorResponse> handleCrewDeleteNameMismatch(CrewDeleteNameMismatchException exception) {
		return error(CrewErrorCode.CREW_DELETE_NAME_MISMATCH);
	}

	@ExceptionHandler(CrewJoinRequestNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleCrewJoinRequestNotFound(CrewJoinRequestNotFoundException exception) {
		return error(CrewErrorCode.CREW_JOIN_REQUEST_NOT_FOUND);
	}

	@ExceptionHandler(ExploreThemeNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleExploreThemeNotFound(ExploreThemeNotFoundException exception) {
		return error(ExploreErrorCode.EXPLORE_THEME_NOT_FOUND);
	}

	@ExceptionHandler(MeetingLogNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingLogNotFound(MeetingLogNotFoundException exception) {
		return error(MeetingLogErrorCode.LOG_NOT_FOUND);
	}

	@ExceptionHandler(MeetingLogAlreadyExistsException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingLogAlreadyExists(MeetingLogAlreadyExistsException exception) {
		return error(MeetingLogErrorCode.LOG_ALREADY_EXISTS);
	}

	@ExceptionHandler(MeetingLogWriteNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingLogWriteNotAllowed(MeetingLogWriteNotAllowedException exception) {
		return error(MeetingLogErrorCode.LOG_WRITE_NOT_ALLOWED);
	}

	@ExceptionHandler(MeetingLogRequestValidationException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingLogRequestValidation(
		MeetingLogRequestValidationException exception
	) {
		return ResponseEntity.badRequest().body(
			apiErrorResponseFactory.create(CommonErrorCode.COMMON_VALIDATION_ERROR, exception.getFieldErrors())
		);
	}

	@ExceptionHandler(MeetingNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingNotFound(MeetingNotFoundException exception) {
		return error(MeetingErrorCode.MEETING_NOT_FOUND);
	}

	@ExceptionHandler(MeetingGalleryNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingGalleryNotFound(MeetingGalleryNotFoundException exception) {
		return error(MeetingGalleryErrorCode.GALLERY_NOT_FOUND);
	}

	@ExceptionHandler(MeetingParticipationAlreadyJoinedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingParticipationAlreadyJoined(
		MeetingParticipationAlreadyJoinedException exception
	) {
		return error(MeetingErrorCode.MEETING_PARTICIPATION_ALREADY_JOINED);
	}

	@ExceptionHandler(MeetingParticipationNotJoinedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingParticipationNotJoined(
		MeetingParticipationNotJoinedException exception
	) {
		return error(MeetingErrorCode.MEETING_PARTICIPATION_NOT_JOINED);
	}

	@ExceptionHandler(MeetingHostCannotCancelParticipationException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingHostCannotCancelParticipation(
		MeetingHostCannotCancelParticipationException exception
	) {
		return error(MeetingErrorCode.MEETING_HOST_CANNOT_CANCEL_PARTICIPATION);
	}

	@ExceptionHandler(MeetingInvalidStatusTransitionException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingInvalidStatusTransition(
		MeetingInvalidStatusTransitionException exception
	) {
		return error(MeetingErrorCode.MEETING_INVALID_STATUS_TRANSITION);
	}

	@ExceptionHandler(MeetingEditNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingEditNotAllowed(
		MeetingEditNotAllowedException exception
	) {
		return error(MeetingErrorCode.MEETING_EDIT_NOT_ALLOWED);
	}

	@ExceptionHandler(MeetingResultRecordNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingResultRecordNotAllowed(
		MeetingResultRecordNotAllowedException exception
	) {
		return error(MeetingErrorCode.MEETING_RESULT_RECORD_NOT_ALLOWED);
	}

	@ExceptionHandler(MeetingResultAlreadyRecordedException.class)
	ResponseEntity<ApiErrorResponse> handleMeetingResultAlreadyRecorded(
		MeetingResultAlreadyRecordedException exception
	) {
		return error(MeetingErrorCode.MEETING_RESULT_ALREADY_RECORDED);
	}

	@ExceptionHandler(AuthUserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleAuthUserNotFound(AuthUserNotFoundException exception) {
		return error(AuthErrorCode.AUTH_USER_NOT_FOUND);
	}

	@ExceptionHandler(UserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException exception) {
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

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		List<ApiErrorField> fieldErrors = exception.getConstraintViolations().stream()
			.map(violation -> new ApiErrorField(
				resolveConstraintField(violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString()),
				violation.getMessage()
			))
			.toList();

		return ResponseEntity.badRequest().body(
			apiErrorResponseFactory.create(CommonErrorCode.COMMON_VALIDATION_ERROR, fieldErrors)
		);
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

	private String resolveConstraintField(String propertyPath) {
		int separator = propertyPath.lastIndexOf('.');
		if (separator < 0 || separator == propertyPath.length() - 1) {
			return propertyPath;
		}
		return propertyPath.substring(separator + 1);
	}
}
