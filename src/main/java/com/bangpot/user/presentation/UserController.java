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
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
class UserController {

	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final GetMyCreatedMeetingsUseCase getMyCreatedMeetingsUseCase;
	private final GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UpdateMyProfileUseCase updateMyProfileUseCase;

	@GetMapping("/api/users/me")
	ResponseEntity<UserDto.UserProfileResponse> profile(Authentication authentication) {
		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(
			GetMyProfileUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/created-meetings")
	ResponseEntity<UserDto.CreatedMeetingsResponse> createdMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		GetMyCreatedMeetingsUseCase.Result result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/joined-meetings")
	ResponseEntity<UserDto.JoinedMeetingsResponse> joinedMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page??0 ?댁긽?댁뼱???⑸땲??") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size??1 ?댁긽?댁뼱???⑸땲??")
		@Max(value = 50, message = "size??50 ?댄븯?ъ빞 ?⑸땲??") int size
	) {
		GetMyJoinedMeetingsUseCase.Result result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
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
