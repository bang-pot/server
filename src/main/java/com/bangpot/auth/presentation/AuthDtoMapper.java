package com.bangpot.auth.presentation;

import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetMyProfileUseCase;
import com.bangpot.auth.application.usecase.UpdateMyProfileUseCase;

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

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		AuthController.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname());
	}

	static AuthController.AuthCompletionResponse toResponse(CompleteTempUserUseCase.Result result) {
		return new AuthController.AuthCompletionResponse(
			result.authStatus().name(),
			result.completionRequired(),
			result.nextPath()
		);
	}

	static AuthController.AuthProfileResponse toResponse(GetMyProfileUseCase.View result) {
		return new AuthController.AuthProfileResponse(result.id(), result.nickname());
	}

	static AuthController.AuthProfileResponse toResponse(UpdateMyProfileUseCase.Result result) {
		return new AuthController.AuthProfileResponse(result.id(), result.nickname());
	}
}
