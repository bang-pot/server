package com.banglog.auth.presentation;

import com.banglog.auth.application.usecase.CompleteTempUserUseCase;

final class AuthDtoMapper {

	private AuthDtoMapper() {
	}

	static CompleteTempUserUseCase.Command toCommand(
		Long userId,
		AuthDto.AuthCompletionRequest request
	) {
		return CompleteTempUserUseCase.Command.of(
			userId,
			request.nickname(),
			request.agreedToRequiredTerms()
		);
	}

	static AuthDto.AuthCompletionResponse toResponse(CompleteTempUserUseCase.Result result) {
		return new AuthDto.AuthCompletionResponse(
			result.authStatus().name(),
			result.completionRequired(),
			result.nextPath()
		);
	}
}
