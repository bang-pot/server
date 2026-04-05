package com.bangpot.common.security;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;
import com.bangpot.auth.error.AuthErrorCode;
import com.bangpot.common.error.ApiErrorResponseWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final AuthAuditLogger authAuditLogger;
	private final ApiErrorResponseWriter apiErrorResponseWriter;

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException, ServletException {
		authAuditLogger.protectedResourceAccessFailed(request.getMethod(), request.getRequestURI());
		apiErrorResponseWriter.write(response, AuthErrorCode.AUTH_UNAUTHENTICATED);
	}
}
