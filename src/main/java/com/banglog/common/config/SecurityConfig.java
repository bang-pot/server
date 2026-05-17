package com.banglog.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.banglog.auth.infrastructure.AuthSessionTokenService;
import com.banglog.auth.infrastructure.config.AuthFrontendProperties;
import com.banglog.auth.infrastructure.config.AuthJwtProperties;
import com.banglog.auth.infrastructure.logging.AuthAuditLogger;
import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.infrastructure.oauth.KakaoOAuth2AuthenticationFailureHandler;
import com.banglog.auth.infrastructure.oauth.KakaoOAuth2AuthenticationSuccessHandler;
import com.banglog.auth.infrastructure.oauth.KakaoOAuth2UserService;
import com.banglog.common.error.ApiErrorResponseWriter;
import com.banglog.common.security.JwtAuthenticationFilter;
import com.banglog.common.security.LoggingAccessDeniedHandler;
import com.banglog.common.security.LoggingAuthenticationEntryPoint;
import com.banglog.common.security.oauth.KakaoAuthorizationRequestResolver;

@Configuration
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(
		AuthJwtProperties authJwtProperties,
		AuthSessionTokenService authSessionTokenService,
		AuthUserRepository authUserRepository
	) {
		return new JwtAuthenticationFilter(authJwtProperties, authSessionTokenService, authUserRepository);
	}

	@Bean
	LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint(
		AuthAuditLogger authAuditLogger,
		ApiErrorResponseWriter apiErrorResponseWriter
	) {
		return new LoggingAuthenticationEntryPoint(authAuditLogger, apiErrorResponseWriter);
	}

	@Bean
	LoggingAccessDeniedHandler loggingAccessDeniedHandler(
		AuthAuditLogger authAuditLogger,
		ApiErrorResponseWriter apiErrorResponseWriter
	) {
		return new LoggingAccessDeniedHandler(authAuditLogger, apiErrorResponseWriter);
	}

	@Bean
	AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
		return new HttpSessionOAuth2AuthorizationRequestRepository();
	}

	@Bean
	KakaoAuthorizationRequestResolver kakaoAuthorizationRequestResolver(
		ClientRegistrationRepository clientRegistrationRepository
	) {
		return new KakaoAuthorizationRequestResolver(clientRegistrationRepository);
	}

	@Bean
	@Order(1)
	SecurityFilterChain oauth2SecurityFilterChain(
		HttpSecurity http,
		KakaoOAuth2UserService kakaoOAuth2UserService,
		KakaoOAuth2AuthenticationSuccessHandler successHandler,
		KakaoOAuth2AuthenticationFailureHandler failureHandler,
		KakaoAuthorizationRequestResolver kakaoAuthorizationRequestResolver,
		AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
		CorsConfigurationSource corsConfigurationSource
	) throws Exception {
		http
			.securityMatcher("/oauth2/**", "/login/oauth2/**")
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.oauth2Login(oauth -> oauth
				.authorizationEndpoint(endpoint -> endpoint
					.authorizationRequestResolver(kakaoAuthorizationRequestResolver)
					.authorizationRequestRepository(authorizationRequestRepository)
				)
				.userInfoEndpoint(endpoint -> endpoint.userService(kakaoOAuth2UserService))
				.successHandler(successHandler)
				.failureHandler(failureHandler)
			);

		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain appSecurityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		CorsConfigurationSource corsConfigurationSource,
		LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint,
		LoggingAccessDeniedHandler loggingAccessDeniedHandler
	) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.oauth2Login(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(
					"/api/auth/me",
					"/api/auth/logout",
					"/api/home",
					"/api/users/nickname-availability",
					"/actuator/health",
					"/actuator/health/**"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/explore/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/crews/explore").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/crews/*/join").permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(loggingAuthenticationEntryPoint)
				.accessDeniedHandler(loggingAccessDeniedHandler)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AuthFrontendProperties authFrontendProperties) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(authFrontendProperties.getBaseUrl()));
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(List.of("Set-Cookie"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
