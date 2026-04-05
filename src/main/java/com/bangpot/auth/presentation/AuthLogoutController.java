package com.bangpot.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.application.usecase.LogoutUseCase;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
class AuthLogoutController {

	private final LogoutUseCase logoutUseCase;
	private final AuthCookieFactory authCookieFactory;

	@PostMapping("/logout")
	ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response) {
		logoutUseCase.handle(LogoutUseCase.Command.of(authenticationUserId(authentication)));
		response.addHeader(HttpHeaders.SET_COOKIE, authCookieFactory.createLogoutCookieHeader());
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

	private Long authenticationUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			return null;
		}
		return userId;
	}
}
