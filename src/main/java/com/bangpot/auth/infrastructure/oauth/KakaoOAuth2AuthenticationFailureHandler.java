package com.bangpot.auth.infrastructure.oauth;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.bangpot.auth.infrastructure.config.AuthFrontendProperties;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class KakaoOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final AuthFrontendProperties authFrontendProperties;
	private final AuthAuditLogger authAuditLogger;

	@Override
	public void onAuthenticationFailure(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception
	) throws IOException, ServletException {
		authAuditLogger.loginFailed("KAKAO", request.getRequestURI(), resolveFailureType(exception));
		response.setStatus(HttpServletResponse.SC_FOUND);
		response.sendRedirect(UriComponentsBuilder
			.fromUriString(authFrontendProperties.getBaseUrl())
			.path("/login")
			.queryParam("error", "oauth_failed")
			.build(true)
			.toUriString());
	}

	private String resolveFailureType(AuthenticationException exception) {
		String simpleName = exception.getClass().getSimpleName();
		return simpleName.isBlank()
			? AuthenticationException.class.getSimpleName()
			: simpleName;
	}
}
