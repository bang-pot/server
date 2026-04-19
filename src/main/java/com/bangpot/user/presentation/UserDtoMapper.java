package com.bangpot.user.presentation;

import java.util.List;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.user.application.exception.UserWithdrawalRequestValidationException;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.domain.WithdrawalReasonCode;

final class UserDtoMapper {

	private UserDtoMapper() {
	}

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		UserDto.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname());
	}

	static UserDto.UserProfileResponse toResponse(GetMyProfileUseCase.View result) {
		return new UserDto.UserProfileResponse(
			result.id(),
			result.nickname(),
			result.profileImageUrl(),
			result.createdMeetingsCount(),
			result.joinedMeetingsCount(),
			result.myCrewsCount(),
			result.pendingCrewsCount()
		);
	}

	static UserDto.UserProfileResponse toResponse(UpdateMyProfileUseCase.Result result) {
		return new UserDto.UserProfileResponse(result.id(), result.nickname(), null, null, null, null, null);
	}

	static UserDto.CreatedMeetingsResponse toResponse(GetMyCreatedMeetingsUseCase.Result result) {
		return new UserDto.CreatedMeetingsResponse(
			result.items().stream()
				.map(item -> new UserDto.CreatedMeetingItemResponse(
					item.meetingId(),
					item.title(),
					item.status(),
					item.date(),
					item.time(),
					item.crewId(),
					item.crewName()
				))
				.toList(),
			new UserDto.CreatedMeetingsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.CalendarResponse toResponse(GetMyCalendarUseCase.Result result) {
		return new UserDto.CalendarResponse(
			result.items().stream()
				.map(item -> new UserDto.CalendarItemResponse(
					item.meetingId(),
					item.meetingTitle(),
					item.crewId(),
					item.crewName(),
					item.date(),
					item.time(),
					item.meetingStatus(),
					item.isCanceled(),
					item.participationRole()
				))
				.toList(),
			result.totalCount()
		);
	}

	static UserDto.JoinedMeetingsResponse toResponse(GetMyJoinedMeetingsUseCase.Result result) {
		return new UserDto.JoinedMeetingsResponse(
			result.items().stream()
				.map(item -> new UserDto.JoinedMeetingItemResponse(
					item.meetingId(),
					item.title(),
					item.themeName(),
					item.crewId(),
					item.crewName(),
					item.date(),
					item.time(),
					item.status(),
					item.result(),
					item.canWriteReview()
				))
				.toList(),
			new UserDto.JoinedMeetingsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.MyCrewsResponse toResponse(GetMyCrewsUseCase.Result result) {
		return new UserDto.MyCrewsResponse(
			result.items().stream()
				.map(item -> new UserDto.MyCrewItemResponse(
					item.crewId(),
					item.crewName(),
					item.visibility(),
					item.leaderNickname(),
					item.coverImageUrl()
				))
				.toList(),
			new UserDto.MyCrewsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.PendingCrewsResponse toResponse(GetMyPendingCrewsUseCase.Result result) {
		return new UserDto.PendingCrewsResponse(
			result.items().stream()
				.map(item -> new UserDto.PendingCrewItemResponse(
					item.joinRequestId(),
					item.crewId(),
					item.crewName(),
					item.requestedAt(),
					item.messageSummary()
				))
				.toList(),
			new UserDto.PendingCrewsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.CancelPendingCrewJoinRequestResponse toResponse(
		CancelMyPendingCrewJoinRequestUseCase.Result result
	) {
		return new UserDto.CancelPendingCrewJoinRequestResponse(result.joinRequestId(), result.crewId());
	}

	static UserDto.WithdrawalCheckResponse toResponse(GetMyWithdrawalCheckUseCase.Result result) {
		return new UserDto.WithdrawalCheckResponse(
			result.canWithdraw(),
			result.blockingActiveCrews().stream()
				.map(crew -> new UserDto.BlockingActiveCrewResponse(
					crew.crewId(),
					crew.crewName()
				))
				.toList(),
			result.blockingParticipatingMeetings().stream()
				.map(meeting -> new UserDto.BlockingParticipatingMeetingResponse(
					meeting.meetingId(),
					meeting.meetingTitle(),
					meeting.crewId(),
					meeting.crewName(),
					meeting.meetingStatus(),
					meeting.date(),
					meeting.time(),
					meeting.participationRole()
				))
				.toList()
		);
	}

	static WithdrawMyAccountUseCase.Command toCommand(
		Long userId,
		UserDto.WithdrawMyAccountRequest request
	) {
		return WithdrawMyAccountUseCase.Command.of(
			userId,
			parseReasonCode(request.reasonCode()),
			normalizeReasonDetail(request.reasonDetail())
		);
	}

	static UserDto.WithdrawMyAccountResponse toResponse(WithdrawMyAccountUseCase.Result result) {
		return new UserDto.WithdrawMyAccountResponse(result.withdrawnAt(), result.canLogin());
	}

	static UserDto.NicknameAvailabilityResponse toResponse(
		CheckNicknameAvailabilityUseCase.Result result
	) {
		return new UserDto.NicknameAvailabilityResponse(result.nickname(), result.available());
	}

	private static WithdrawalReasonCode parseReasonCode(String reasonCode) {
		try {
			return WithdrawalReasonCode.valueOf(reasonCode.trim());
		} catch (RuntimeException exception) {
			throw new UserWithdrawalRequestValidationException(
				List.of(new ApiErrorField("reasonCode", "reasonCode is invalid"))
			);
		}
	}

	private static String normalizeReasonDetail(String reasonDetail) {
		if (reasonDetail == null) {
			return null;
		}
		String trimmed = reasonDetail.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
