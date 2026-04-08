package com.bangpot.auth.presentation;

import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetMyProfileUseCase;
import com.bangpot.auth.application.usecase.UpdateMyProfileUseCase;

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

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		AuthDto.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname());
	}

	static AuthDto.AuthCompletionResponse toResponse(CompleteTempUserUseCase.Result result) {
		return new AuthDto.AuthCompletionResponse(
			result.authStatus().name(),
			result.completionRequired(),
			result.nextPath()
		);
	}

	static AuthDto.AuthProfileResponse toResponse(GetMyProfileUseCase.View result) {
		return new AuthDto.AuthProfileResponse(result.id(), result.nickname());
	}

	static AuthDto.AuthProfileResponse toResponse(UpdateMyProfileUseCase.Result result) {
		return new AuthDto.AuthProfileResponse(result.id(), result.nickname());
	}
}
