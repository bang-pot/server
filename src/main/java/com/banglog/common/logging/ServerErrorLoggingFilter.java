package com.banglog.common.logging;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ServerErrorLoggingFilter extends OncePerRequestFilter {

	private static final String SERVER_ERROR_MESSAGE = "\uC694\uCCAD \uCC98\uB9AC \uC911 \uC11C\uBC84 \uC624\uB958 \uBC1C\uC0DD";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (RuntimeException | ServletException | IOException exception) {
			logServerError(request, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getClass().getSimpleName());
			throw exception;
		}

		if (!isHealthEndpoint(request.getRequestURI()) && response.getStatus() >= 500) {
			logServerError(request, response.getStatus(), "HttpStatus" + response.getStatus());
		}
	}

	private void logServerError(HttpServletRequest request, int status, String exceptionType) {
		if (isHealthEndpoint(request.getRequestURI())) {
			return;
		}
		log.error(
			"event=request.failed message=\"{}\" requestId={} method={} path={} status={} exceptionType={}",
			SERVER_ERROR_MESSAGE,
			RequestTrace.currentRequestId(),
			request.getMethod(),
			RequestTrace.sanitizePath(request.getRequestURI()),
			status,
			exceptionType
		);
	}

	private boolean isHealthEndpoint(String path) {
		String sanitizedPath = RequestTrace.sanitizePath(path);
		return sanitizedPath.equals("/actuator/health") || sanitizedPath.startsWith("/actuator/health/");
	}
}
