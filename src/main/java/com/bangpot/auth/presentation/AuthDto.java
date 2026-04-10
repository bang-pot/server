package com.bangpot.auth.presentation;

import jakarta.validation.constraints.NotBlank;

final class AuthDto {

	private AuthDto() {
	}

	record AuthCompletionResponse(
		String authStatus,
		boolean completionRequired,
		String nextPath
	) {
	}

	record AuthCompletionRequest(
		@NotBlank(message = "닉네임은 비어 있을 수 없습니다.") String nickname,
		boolean agreedToRequiredTerms
	) {
	}
}
