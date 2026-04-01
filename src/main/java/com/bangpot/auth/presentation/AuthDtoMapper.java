package com.bangpot.auth.presentation;

import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;

final class AuthDtoMapper {

	private AuthDtoMapper() {
	}

	static CompleteTempUserUseCase.Command toCommand(
		Long userId,
		AuthController.AuthCompletionRequest request
	) {
		return CompleteTempUserUseCase.Command.of(
			userId,
			request.nickname(),
			request.agreedToRequiredTerms()
		);
	}

	static AuthController.AuthCompletionResponse toResponse(CompleteTempUserUseCase.Result result) {
		return new AuthController.AuthCompletionResponse(
			result.authStatus().name(),
			result.completionRequired(),
			result.nextPath()
		);
	}
}
