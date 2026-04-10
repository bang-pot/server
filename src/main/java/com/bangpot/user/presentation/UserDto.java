package com.bangpot.user.presentation;

import jakarta.validation.constraints.NotBlank;

final class UserDto {

	private UserDto() {
	}

	record UpdateMyProfileRequest(
		@NotBlank(message = "닉네임은 비어 있을 수 없습니다.") String nickname
	) {
	}

	record UserProfileResponse(Long id, String nickname) {
	}

	record NicknameAvailabilityResponse(String nickname, boolean available) {
	}
}
