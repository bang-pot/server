package com.bangpot.user.presentation;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

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

	record NicknameAvailabilityResponse(String nickname, boolean available) {
	}
}
