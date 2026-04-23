package com.bangpot.user.presentation;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

	record FavoriteThemeItemResponse(
		Long themeId,
		String themeName,
		String storeName,
		String regionName,
		String thumbnailUrl,
		Integer favoriteCount,
		boolean isFavorite
	) {
	}

	record FavoriteThemesPageInfo(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record FavoriteThemesResponse(
		List<FavoriteThemeItemResponse> items,
		FavoriteThemesPageInfo pageInfo
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

	record UserSearchItemResponse(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount
	) {
	}

	record UserSearchPageInfo(
		int page,
		int size,
		long totalElements,
		int totalPages
	) {
	}

	record UserSearchResponse(
		List<UserSearchItemResponse> items,
		UserSearchPageInfo pageInfo
	) {
	}

	record BlockingActiveCrewResponse(
		Long crewId,
		String crewName
	) {
	}

	record WithdrawalCheckResponse(
		boolean canWithdraw,
		List<BlockingActiveCrewResponse> blockingActiveCrews
	) {
	}

	record WithdrawMyAccountRequest(
		@NotBlank(message = "탈퇴 사유를 선택해 주세요.") String reasonCode,
		@Size(max = 500, message = "탈퇴 상세 사유는 500자 이하여야 합니다.") String reasonDetail,
		@AssertTrue(message = "탈퇴 안내 사항에 동의해 주세요.") boolean confirmationChecked
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
