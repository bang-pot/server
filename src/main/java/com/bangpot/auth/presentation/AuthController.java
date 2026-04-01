package com.bangpot.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;

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

	@GetMapping("/me")
	ResponseEntity<GetCurrentAuthUserUseCase.View> me(Authentication authentication) {
		Long userId = authentication == null ? null : (Long) authentication.getPrincipal();
		return ResponseEntity.ok(getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(userId)
		));
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
		if (authentication == null || authentication.getPrincipal() == null) {
			throw new UnauthenticatedException();
		}
		Long userId = (Long) authentication.getPrincipal();
		CompleteTempUserUseCase.Result result = completeTempUserUseCase.handle(
			AuthDtoMapper.toCommand(userId, completionRequest)
		);
		return ResponseEntity.ok(AuthDtoMapper.toResponse(result));
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
