package com.banglog.common.security.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.banglog.auth.infrastructure.RedirectPathSanitizer;

import jakarta.servlet.http.HttpServletRequest;

public class KakaoAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	public static final String REDIRECT_TO_ATTRIBUTE = "banglog.redirectTo";

	private final DefaultOAuth2AuthorizationRequestResolver delegate;

	public KakaoAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
			clientRegistrationRepository,
			"/oauth2/authorization"
		);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return customize(request, delegate.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		return customize(request, delegate.resolve(request, clientRegistrationId));
	}

	private OAuth2AuthorizationRequest customize(
		HttpServletRequest request,
		OAuth2AuthorizationRequest authorizationRequest
	) {
		if (authorizationRequest == null) {
			return null;
		}

		String redirectTo = RedirectPathSanitizer.sanitize(request.getParameter("redirectTo"));
		if (redirectTo == null) {
			return authorizationRequest;
		}

		return OAuth2AuthorizationRequest.from(authorizationRequest)
			.attributes(attributes -> attributes.put(REDIRECT_TO_ATTRIBUTE, redirectTo))
			.build();
	}
}
