package com.banglog.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import com.banglog.auth.infrastructure.logging.AuthAuditLogger;
import com.banglog.common.error.ApiErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

class SecurityFailureHandlersTest {

	@Test
	void returnsUnauthorizedAndLogsProtectedResourceAccessFailure() throws Exception {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint(
			authAuditLogger,
			new com.banglog.common.error.ApiErrorResponseWriter(new ObjectMapper(), new ApiErrorResponseFactory())
		);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/crew");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MDC.put("requestId", "req-auth-401");

		try {
			entryPoint.commence(request, response, new AuthenticationException("missing auth") {
			});
		} finally {
			MDC.clear();
		}

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).startsWith("application/json");
		assertThat(response.getContentAsString())
			.contains("\"code\":\"AUTH_UNAUTHENTICATED\"")
			.contains("\"requestId\":\"req-auth-401\"")
			.contains("\"fieldErrors\":[]");
		verify(authAuditLogger).protectedResourceAccessFailed("GET", "/api/crew");
	}

	@Test
	void returnsForbiddenAndLogsAccessDenied() throws Exception {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler(
			authAuditLogger,
			new com.banglog.common.error.ApiErrorResponseWriter(new ObjectMapper(), new ApiErrorResponseFactory())
		);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin");
		request.setUserPrincipal(new UsernamePasswordAuthenticationToken(55L, null, List.of()));
		MockHttpServletResponse response = new MockHttpServletResponse();
		MDC.put("requestId", "req-auth-403");

		try {
			handler.handle(
				request,
				response,
				new AccessDeniedException("forbidden")
			);
		} finally {
			MDC.clear();
		}

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentType()).startsWith("application/json");
		assertThat(response.getContentAsString())
			.contains("\"code\":\"AUTH_ACCESS_DENIED\"")
			.contains("\"requestId\":\"req-auth-403\"")
			.contains("\"fieldErrors\":[]");
		verify(authAuditLogger).accessDenied(55L, "POST", "/api/admin");
	}
}
