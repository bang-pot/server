package com.banglog.user.presentation;

import java.util.List;

import com.banglog.common.error.ApiErrorField;
import com.banglog.explore.domain.view.MyFavoriteThemesView;
import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.MyPendingCrewsView;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.meeting.domain.view.MyMeetingLogsView;
import com.banglog.user.application.exception.UserWithdrawalRequestValidationException;
import com.banglog.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.banglog.user.application.usecase.UpdateMyProfileUseCase;
import com.banglog.user.application.usecase.WithdrawMyAccountUseCase;
import com.banglog.user.domain.WithdrawalReasonCode;
import com.banglog.user.domain.view.MyProfileView;
import com.banglog.user.domain.view.MyWithdrawalCheckView;
import com.banglog.user.domain.view.UserSearchView;

final class UserDtoMapper {

	private UserDtoMapper() {
	}

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		UserDto.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname(), request.profileImageUploadId());
	}

	static UserDto.UserProfileResponse toResponse(MyProfileView result) {
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

	static UserDto.CreatedMeetingsResponse toResponse(MyCreatedMeetingsView result) {
		return new UserDto.CreatedMeetingsResponse(
			result.items().stream()
				.map(item -> new UserDto.CreatedMeetingItemResponse(
					item.meetingId(),
					item.title(),
					item.status().name(),
					item.date(),
					item.time(),
					item.crewId(),
					item.crewName()
				))
				.toList(),
			new UserDto.CreatedMeetingsPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.CalendarResponse toResponse(MyCalendarView result) {
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

	static UserDto.FavoriteThemeSummaryResponse toResponse(MyFavoriteThemesSummaryView result) {
		return new UserDto.FavoriteThemeSummaryResponse(
			result.items().stream()
				.map(item -> new UserDto.FavoriteThemeSummaryItemResponse(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionName(),
					item.thumbnailUrl(),
					item.favoriteCount(),
					item.isFavorite()
				))
				.toList(),
			result.totalCount(),
			result.hasMore()
		);
	}

	static UserDto.FavoriteThemesResponse toResponse(MyFavoriteThemesView result) {
		return new UserDto.FavoriteThemesResponse(
			result.items().stream()
				.map(item -> new UserDto.FavoriteThemeItemResponse(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionName(),
					item.thumbnailUrl(),
					item.genreName(),
					item.difficulty(),
					item.runningTimeMinutes(),
					item.description(),
					item.favoriteCount(),
					item.isFavorite()
				))
				.toList(),
			new UserDto.FavoriteThemesPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.MyMeetingLogsResponse toResponse(MyMeetingLogsView result) {
		return new UserDto.MyMeetingLogsResponse(
			result.items().stream()
				.map(item -> new UserDto.MyMeetingLogItemResponse(
					item.logId(),
					item.crewId(),
					item.crewName(),
					item.meetingId(),
					item.meetingTitle(),
					item.meetingDate(),
					item.createdAt(),
					item.result().name(),
					item.excerpt(),
					item.coverPhotoUrl(),
					item.photoCount()
				))
				.toList(),
			new UserDto.MyMeetingLogsPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.JoinedMeetingsResponse toResponse(MyJoinedMeetingsView result) {
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
					item.status().name(),
					item.canWriteReview()
				))
				.toList(),
			new UserDto.JoinedMeetingsPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.MyCrewsResponse toResponse(MyCrewsView result) {
		return new UserDto.MyCrewsResponse(
			result.items().stream()
				.map(item -> new UserDto.MyCrewItemResponse(
					item.crewId(),
					item.crewName(),
					item.description(),
					item.visibility().name(),
					item.leaderNickname(),
					item.coverImageUrl(),
					item.myRole().name(),
					item.memberCount()
				))
				.toList(),
			new UserDto.MyCrewsPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.PendingCrewsResponse toResponse(MyPendingCrewsView result) {
		return new UserDto.PendingCrewsResponse(
			result.items().stream()
				.map(item -> new UserDto.PendingCrewItemResponse(
					item.joinRequestId(),
					item.crewId(),
					item.crewName(),
					item.description(),
					item.visibility().name(),
					item.leaderNickname(),
					item.coverImageUrl(),
					item.memberCount(),
					item.requestedAt(),
					item.messageSummary()
				))
				.toList(),
			new UserDto.PendingCrewsPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().hasNext()
			)
		);
	}

	static UserDto.UserSearchResponse toResponse(UserSearchView result) {
		return new UserDto.UserSearchResponse(
			result.items().stream()
				.map(item -> new UserDto.UserSearchItemResponse(
					item.userId(),
					item.nickname(),
					item.profileImageUrl(),
					item.bio(),
					item.gender(),
					item.escapeCount()
				))
				.toList(),
			new UserDto.UserSearchPageInfo(
				result.page().page(),
				result.page().size(),
				result.page().totalElements(),
				result.page().totalPages()
			)
		);
	}

	static UserDto.WithdrawalCheckResponse toResponse(MyWithdrawalCheckView result) {
		return new UserDto.WithdrawalCheckResponse(
			result.canWithdraw(),
			result.blockingActiveCrews().stream()
				.map(crew -> new UserDto.BlockingActiveCrewResponse(
					crew.crewId(),
					crew.crewName()
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
		return new UserDto.WithdrawMyAccountResponse(result.withdrawnAt().toString(), result.canLogin());
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
				List.of(new ApiErrorField("reasonCode", "유효하지 않은 탈퇴 사유입니다."))
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
