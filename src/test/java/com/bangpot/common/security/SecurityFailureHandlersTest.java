package com.bangpot.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;

class SecurityFailureHandlersTest {

	@Test
	void returnsUnauthorizedAndLogsProtectedResourceAccessFailure() throws Exception {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint(authAuditLogger);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/crew");
		MockHttpServletResponse response = new MockHttpServletResponse();

		entryPoint.commence(request, response, new AuthenticationException("missing auth") {
		});

		assertThat(response.getStatus()).isEqualTo(401);
		verify(authAuditLogger).protectedResourceAccessFailed("GET", "/api/crew");
	}

	@Test
	void returnsForbiddenAndLogsAccessDenied() throws Exception {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler(authAuditLogger);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin");
		request.setUserPrincipal(new UsernamePasswordAuthenticationToken(55L, null, List.of()));
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.handle(
			request,
			response,
			new AccessDeniedException("forbidden")
		);

		assertThat(response.getStatus()).isEqualTo(403);
		verify(authAuditLogger).accessDenied(55L, "POST", "/api/admin");
	}
}
