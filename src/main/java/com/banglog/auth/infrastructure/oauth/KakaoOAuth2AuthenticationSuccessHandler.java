package com.banglog.auth.infrastructure.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.banglog.auth.application.usecase.LoginWithProviderUseCase;
import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.infrastructure.AuthSessionTokenService;
import com.banglog.auth.infrastructure.config.AuthFrontendProperties;
import com.banglog.auth.infrastructure.config.AuthJwtProperties;
import com.banglog.common.security.oauth.KakaoAuthorizationRequestResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class KakaoOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final LoginWithProviderUseCase loginWithProviderUseCase;
	private final AuthSessionTokenService authSessionTokenService;
	private final AuthJwtProperties authJwtProperties;
	private final AuthFrontendProperties authFrontendProperties;
	private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {
		KakaoOAuth2User user = (KakaoOAuth2User)authentication.getPrincipal();
		String redirectTo = resolveRedirectTo(request, response);
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(
				AuthProvider.KAKAO,
				String.valueOf(user.providerId()),
				redirectTo
			)
		);
		String accessToken = authSessionTokenService.createAccessToken(loginResult.userId());

		ResponseCookie accessTokenCookie = ResponseCookie.from(
			authJwtProperties.getCookieName(),
			accessToken
		)
			.httpOnly(true)
			.secure(authJwtProperties.isSecureCookie())
			.path("/")
			.sameSite("Lax")
			.maxAge(Duration.ofSeconds(authJwtProperties.getAccessTokenValiditySeconds()))
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		response.sendRedirect(resolveFrontendDestination(loginResult));
	}

	private String resolveRedirectTo(HttpServletRequest request, HttpServletResponse response) {
		OAuth2AuthorizationRequest authorizationRequest =
			authorizationRequestRepository.removeAuthorizationRequest(request, response);

		if (authorizationRequest == null) {
			return null;
		}

		Object redirectTo = authorizationRequest.getAttributes()
			.get(KakaoAuthorizationRequestResolver.REDIRECT_TO_ATTRIBUTE);
		return redirectTo instanceof String path ? path : null;
	}

	private String resolveFrontendDestination(LoginWithProviderUseCase.Result loginResult) {
		UriComponentsBuilder builder = UriComponentsBuilder
			.fromUriString(authFrontendProperties.getBaseUrl());

		if (loginResult.completionRequired()) {
			builder.path("/auth/complete");
			if (loginResult.pendingRedirectPath() != null) {
				builder.queryParam(
					"redirectTo",
					URLEncoder.encode(loginResult.pendingRedirectPath(), StandardCharsets.UTF_8)
				);
			}
			return builder.build(true).toUriString();
		}

		return builder.path(loginResult.nextPath()).build(true).toUriString();
	}
}
