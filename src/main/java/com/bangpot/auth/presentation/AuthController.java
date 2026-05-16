package com.bangpot.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.common.idempotency.Idempotent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
class AuthController {

	private final GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;
	private final CompleteTempUserUseCase completeTempUserUseCase;

	@GetMapping("/me")
	ResponseEntity<GetCurrentAuthUserUseCase.View> me(Authentication authentication) {
		Long userId = authentication == null ? null : (Long) authentication.getPrincipal();
		return ResponseEntity.ok(getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(userId)
		));
	}

	@PostMapping("/complete")
	@Idempotent
	ResponseEntity<AuthDto.AuthCompletionResponse> complete(
		Authentication authentication,
		@Valid @RequestBody AuthDto.AuthCompletionRequest completionRequest
	) {
		CompleteTempUserUseCase.Result result = completeTempUserUseCase.handle(
			AuthDtoMapper.toCommand(requireAuthenticatedUserId(authentication), completionRequest)
		);
		return ResponseEntity.ok(AuthDtoMapper.toResponse(result));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
