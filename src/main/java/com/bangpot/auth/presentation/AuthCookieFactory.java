package com.bangpot.auth.presentation;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.bangpot.auth.infrastructure.config.AuthJwtProperties;

@Component
@RequiredArgsConstructor
class AuthCookieFactory {

	private final AuthJwtProperties authJwtProperties;

	String createLogoutCookieHeader() {
		return ResponseCookie.from(authJwtProperties.getCookieName(), "")
			.httpOnly(true)
			.secure(authJwtProperties.isSecureCookie())
			.path("/")
			.sameSite("Lax")
			.maxAge(Duration.ZERO)
			.build()
			.toString();
	}
}
