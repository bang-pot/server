package com.bangpot.common.logging;

import java.io.IOException;

import org.slf4j.MDC;
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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

	private static final long SLOW_REQUEST_THRESHOLD_MS = 3_000L;
	private static final String REQUEST_COMPLETED_MESSAGE = "\uC694\uCCAD \uCC98\uB9AC \uC644\uB8CC";
	private static final String SLOW_REQUEST_MESSAGE = "\uB290\uB9B0 \uC694\uCCAD \uAC10\uC9C0";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String requestId = RequestTrace.resolveRequestId(request.getHeader(RequestTrace.REQUEST_ID_HEADER));
		long startedAt = currentNanoTime();

		MDC.put(RequestTrace.REQUEST_ID_MDC_KEY, requestId);
		response.setHeader(RequestTrace.REQUEST_ID_HEADER, requestId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			long durationMs = (currentNanoTime() - startedAt) / 1_000_000;
			String sanitizedPath = RequestTrace.sanitizePath(request.getRequestURI());
			log.info(
				"event=request.completed message=\"{}\" requestId={} method={} path={} status={} durationMs={}",
				REQUEST_COMPLETED_MESSAGE,
				requestId,
				request.getMethod(),
				sanitizedPath,
				response.getStatus(),
				durationMs
			);
			if (durationMs >= SLOW_REQUEST_THRESHOLD_MS) {
				log.warn(
					"event=request.slow message=\"{}\" requestId={} method={} path={} status={} durationMs={} thresholdMs={} context=request.slow",
					SLOW_REQUEST_MESSAGE,
					requestId,
					request.getMethod(),
					sanitizedPath,
					response.getStatus(),
					durationMs,
					SLOW_REQUEST_THRESHOLD_MS
				);
			}
			MDC.remove(RequestTrace.REQUEST_ID_MDC_KEY);
		}
	}

	long currentNanoTime() {
		return System.nanoTime();
	}
}
