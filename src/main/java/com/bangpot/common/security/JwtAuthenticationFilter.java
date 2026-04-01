package com.bangpot.common.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bangpot.auth.infrastructure.AuthSessionTokenService;
import com.bangpot.auth.infrastructure.config.AuthJwtProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final AuthJwtProperties authJwtProperties;
	private final AuthSessionTokenService authSessionTokenService;

	public JwtAuthenticationFilter(
		AuthJwtProperties authJwtProperties,
		AuthSessionTokenService authSessionTokenService
	) {
		this.authJwtProperties = authJwtProperties;
		this.authSessionTokenService = authSessionTokenService;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		resolveAccessToken(request)
			.flatMap(authSessionTokenService::resolveUserId)
			.ifPresent(userId -> SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of())
			));

		filterChain.doFilter(request, response);
	}

	private Optional<String> resolveAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Bearer ")) {
			return Optional.of(authorization.substring(7));
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> authJwtProperties.getCookieName().equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}
}
