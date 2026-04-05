package com.bangpot.common.config;

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

import com.bangpot.auth.infrastructure.AuthSessionTokenService;
import com.bangpot.auth.infrastructure.config.AuthFrontendProperties;
import com.bangpot.auth.infrastructure.config.AuthJwtProperties;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;
import com.bangpot.auth.infrastructure.oauth.KakaoOAuth2AuthenticationFailureHandler;
import com.bangpot.auth.infrastructure.oauth.KakaoOAuth2AuthenticationSuccessHandler;
import com.bangpot.auth.infrastructure.oauth.KakaoOAuth2UserService;
import com.bangpot.common.security.JwtAuthenticationFilter;
import com.bangpot.common.security.LoggingAccessDeniedHandler;
import com.bangpot.common.security.LoggingAuthenticationEntryPoint;
import com.bangpot.common.security.oauth.KakaoAuthorizationRequestResolver;

@Configuration
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(
		AuthJwtProperties authJwtProperties,
		AuthSessionTokenService authSessionTokenService
	) {
		return new JwtAuthenticationFilter(authJwtProperties, authSessionTokenService);
	}

	@Bean
	LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint(AuthAuditLogger authAuditLogger) {
		return new LoggingAuthenticationEntryPoint(authAuditLogger);
	}

	@Bean
	LoggingAccessDeniedHandler loggingAccessDeniedHandler(AuthAuditLogger authAuditLogger) {
		return new LoggingAccessDeniedHandler(authAuditLogger);
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
					"/api/auth/nickname-availability",
					"/actuator/health",
					"/actuator/health/**"
				).permitAll()
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
		configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(List.of("Set-Cookie"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
