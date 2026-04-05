package com.bangpot.common.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final AuthAuditLogger authAuditLogger;

	public LoggingAuthenticationEntryPoint(AuthAuditLogger authAuditLogger) {
		this.authAuditLogger = authAuditLogger;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException, ServletException {
		authAuditLogger.protectedResourceAccessFailed(request.getMethod(), request.getRequestURI());
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}
