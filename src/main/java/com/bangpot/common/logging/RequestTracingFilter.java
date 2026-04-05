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

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String requestId = RequestTrace.resolveRequestId(request.getHeader(RequestTrace.REQUEST_ID_HEADER));
		long startedAt = System.nanoTime();

		MDC.put(RequestTrace.REQUEST_ID_MDC_KEY, requestId);
		response.setHeader(RequestTrace.REQUEST_ID_HEADER, requestId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
			log.info(
				"event=request.completed message=\"요청 처리 완료\" requestId={} method={} path={} status={} durationMs={}",
				requestId,
				request.getMethod(),
				RequestTrace.sanitizePath(request.getRequestURI()),
				response.getStatus(),
				durationMs
			);
			MDC.remove(RequestTrace.REQUEST_ID_MDC_KEY);
		}
	}
}
