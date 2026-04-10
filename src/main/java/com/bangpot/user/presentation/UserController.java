package com.bangpot.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
class UserController {

	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UpdateMyProfileUseCase updateMyProfileUseCase;

	@GetMapping("/api/users/me")
	ResponseEntity<UserDto.UserProfileResponse> profile(Authentication authentication) {
		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(
			GetMyProfileUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/nickname-availability")
	ResponseEntity<UserDto.NicknameAvailabilityResponse> nicknameAvailability(
		@RequestParam("nickname") String nickname
	) {
		return ResponseEntity.ok(UserDtoMapper.toResponse(
			checkNicknameAvailabilityUseCase.handle(CheckNicknameAvailabilityUseCase.Query.of(nickname))
		));
	}

	@PatchMapping("/api/users/me")
	ResponseEntity<UserDto.UserProfileResponse> updateProfile(
		Authentication authentication,
		@Valid @RequestBody UserDto.UpdateMyProfileRequest request
	) {
		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			UserDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
