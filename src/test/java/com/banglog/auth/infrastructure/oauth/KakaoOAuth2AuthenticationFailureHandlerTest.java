package com.banglog.auth.infrastructure.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import com.banglog.auth.infrastructure.config.AuthFrontendProperties;
import com.banglog.auth.infrastructure.logging.AuthAuditLogger;

class KakaoOAuth2AuthenticationFailureHandlerTest {

	private AuthAuditLogger authAuditLogger;
	private KakaoOAuth2AuthenticationFailureHandler handler;

	@BeforeEach
	void setUp() {
		authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		AuthFrontendProperties authFrontendProperties = new AuthFrontendProperties();
		authFrontendProperties.setBaseUrl("http://localhost:3000");
		handler = new KakaoOAuth2AuthenticationFailureHandler(
			authFrontendProperties,
			authAuditLogger
		);
	}

	@Test
	void redirectsToFrontendLoginAndLogsFailure() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/kakao");
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationFailure(request, response, new AuthenticationException("oauth failed") {
		});

		assertThat(response.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/login?error=oauth_failed");
		verify(authAuditLogger).loginFailed("KAKAO", "/login/oauth2/code/kakao", "AuthenticationException");
	}
}
