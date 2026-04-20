package com.bangpot.user.presentation;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;

final class UserDto {

	private UserDto() {
	}

	record UpdateMyProfileRequest(
		@NotBlank(message = "닉네임은 비어 있을 수 없습니다.") String nickname
	) {
	}

	record UserProfileResponse(
		Long id,
		String nickname,
		String profileImageUrl,
		Long createdMeetingsCount,
		Long joinedMeetingsCount,
		Long myCrewsCount,
		Long pendingCrewsCount
	) {
	}

	record CreatedMeetingItemResponse(
		Long meetingId,
		String title,
		String status,
		String date,
		String time,
		Long crewId,
		String crewName
	) {
	}

	record CreatedMeetingsPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record CreatedMeetingsResponse(
		List<CreatedMeetingItemResponse> items,
		CreatedMeetingsPageInfo pageInfo
	) {
	}

	record CalendarItemResponse(
		Long meetingId,
		String meetingTitle,
		Long crewId,
		String crewName,
		String date,
		String time,
		String meetingStatus,
		boolean isCanceled,
		String participationRole
	) {
	}

	record CalendarResponse(
		List<CalendarItemResponse> items,
		int totalCount
	) {
	}

	record FavoriteThemeSummaryItemResponse(
		Long themeId,
		String themeName,
		String storeName,
		String regionName,
		String thumbnailUrl,
		Integer favoriteCount,
		boolean isFavorite
	) {
	}

	record FavoriteThemeSummaryResponse(
		List<FavoriteThemeSummaryItemResponse> items,
		Long totalCount,
		boolean hasMore
	) {
	}

	record MyMeetingLogItemResponse(
		Long logId,
		Long crewId,
		String crewName,
		Long meetingId,
		String meetingTitle,
		String meetingDate,
		Instant createdAt,
		String excerpt,
		String coverPhotoUrl,
		Long photoCount
	) {
	}

	record MyMeetingLogsPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record MyMeetingLogsResponse(
		List<MyMeetingLogItemResponse> items,
		MyMeetingLogsPageInfo pageInfo
	) {
	}

	record JoinedMeetingItemResponse(
		Long meetingId,
		String title,
		String themeName,
		Long crewId,
		String crewName,
		String date,
		String time,
		String status,
		String result,
		boolean canWriteReview
	) {
	}

	record JoinedMeetingsPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record JoinedMeetingsResponse(
		List<JoinedMeetingItemResponse> items,
		JoinedMeetingsPageInfo pageInfo
	) {
	}

	record MyCrewItemResponse(
		Long crewId,
		String crewName,
		String visibility,
		String leaderNickname,
		String coverImageUrl
	) {
	}

	record MyCrewsPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record MyCrewsResponse(
		List<MyCrewItemResponse> items,
		MyCrewsPageInfo pageInfo
	) {
	}

	record PendingCrewItemResponse(
		Long joinRequestId,
		Long crewId,
		String crewName,
		String requestedAt,
		String messageSummary
	) {
	}

	record PendingCrewsPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record PendingCrewsResponse(
		List<PendingCrewItemResponse> items,
		PendingCrewsPageInfo pageInfo
	) {
	}

	record CancelPendingCrewJoinRequestResponse(
		Long joinRequestId,
		Long crewId
	) {
	}

	record BlockingActiveCrewResponse(
		Long crewId,
		String crewName
	) {
	}

	record BlockingParticipatingMeetingResponse(
		Long meetingId,
		String meetingTitle,
		Long crewId,
		String crewName,
		String meetingStatus,
		String date,
		String time,
		String participationRole
	) {
	}

	record WithdrawalCheckResponse(
		boolean canWithdraw,
		List<BlockingActiveCrewResponse> blockingActiveCrews,
		List<BlockingParticipatingMeetingResponse> blockingParticipatingMeetings
	) {
	}

	record WithdrawMyAccountRequest(
		@NotBlank(message = "reasonCode is required") String reasonCode,
		String reasonDetail,
		@AssertTrue(message = "confirmationChecked must be true") boolean confirmationChecked
	) {
	}

	record WithdrawMyAccountResponse(
		String withdrawnAt,
		boolean canLogin
	) {
	}

	record NicknameAvailabilityResponse(String nickname, boolean available) {
	}
}
