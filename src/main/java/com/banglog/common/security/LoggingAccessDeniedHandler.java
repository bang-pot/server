package com.banglog.common.security;

import java.io.IOException;
import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.banglog.auth.infrastructure.logging.AuthAuditLogger;
import com.banglog.auth.error.AuthErrorCode;
import com.banglog.common.error.ApiErrorResponseWriter;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

	private final AuthAuditLogger authAuditLogger;
	private final ApiErrorResponseWriter apiErrorResponseWriter;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException, ServletException {
		authAuditLogger.accessDenied(resolveUserId(request.getUserPrincipal()), request.getMethod(), request.getRequestURI());
		apiErrorResponseWriter.write(response, AuthErrorCode.AUTH_ACCESS_DENIED);
	}

	private Long resolveUserId(Principal principal) {
		if (principal instanceof Authentication authentication
			&& authentication.getPrincipal() instanceof Long userId) {
			return userId;
		}
		return null;
	}
}
