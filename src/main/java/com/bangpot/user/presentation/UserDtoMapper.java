package com.bangpot.user.presentation;

import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;

final class UserDtoMapper {

	private UserDtoMapper() {
	}

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		UserDto.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname());
	}

	static UserDto.UserProfileResponse toResponse(GetMyProfileUseCase.View result) {
		return new UserDto.UserProfileResponse(result.id(), result.nickname());
	}

	static UserDto.UserProfileResponse toResponse(UpdateMyProfileUseCase.Result result) {
		return new UserDto.UserProfileResponse(result.id(), result.nickname());
	}

	static UserDto.NicknameAvailabilityResponse toResponse(
		CheckNicknameAvailabilityUseCase.Result result
	) {
		return new UserDto.NicknameAvailabilityResponse(result.nickname(), result.available());
	}
}
