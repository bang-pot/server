package com.bangpot.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.application.usecase.GetMyProfileUseCase;
import com.bangpot.auth.application.usecase.UpdateMyProfileUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
class AuthController {

	private final GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;
	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final CompleteTempUserUseCase completeTempUserUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UpdateMyProfileUseCase updateMyProfileUseCase;

	@GetMapping("/me")
	ResponseEntity<GetCurrentAuthUserUseCase.View> me(Authentication authentication) {
		Long userId = authentication == null ? null : (Long) authentication.getPrincipal();
		return ResponseEntity.ok(getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(userId)
		));
	}

	@GetMapping("/profile")
	ResponseEntity<AuthProfileResponse> profile(Authentication authentication) {
		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(
			GetMyProfileUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(AuthDtoMapper.toResponse(result));
	}

	@GetMapping("/nickname-availability")
	ResponseEntity<CheckNicknameAvailabilityUseCase.Result> nicknameAvailability(
		@RequestParam("nickname") String nickname
	) {
		return ResponseEntity.ok(checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of(nickname)
		));
	}

	@PostMapping("/complete")
	ResponseEntity<AuthCompletionResponse> complete(
		Authentication authentication,
		@Valid @RequestBody AuthCompletionRequest completionRequest
	) {
		CompleteTempUserUseCase.Result result = completeTempUserUseCase.handle(
			AuthDtoMapper.toCommand(requireAuthenticatedUserId(authentication), completionRequest)
		);
		return ResponseEntity.ok(AuthDtoMapper.toResponse(result));
	}

	@PatchMapping("/profile")
	ResponseEntity<AuthProfileResponse> updateProfile(
		Authentication authentication,
		@Valid @RequestBody UpdateMyProfileRequest request
	) {
		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			AuthDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(AuthDtoMapper.toResponse(result));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
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

	record UpdateMyProfileRequest(
		@NotBlank(message = "닉네임은 비어 있을 수 없습니다.") String nickname
	) {
	}

	record AuthProfileResponse(Long id, String nickname) {
	}
}
