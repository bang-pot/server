package com.banglog.common.idempotency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.banglog.common.error.ApiErrorResponseWriter;

@Configuration
@ConditionalOnBean(IdempotencyKeyStore.class)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyWebConfig implements WebMvcConfigurer {

	private final IdempotencyKeyStore idempotencyKeyStore;
	private final IdempotencyProperties properties;
	private final ApiErrorResponseWriter apiErrorResponseWriter;

	public IdempotencyWebConfig(
		IdempotencyKeyStore idempotencyKeyStore,
		IdempotencyProperties properties,
		ApiErrorResponseWriter apiErrorResponseWriter
	) {
		this.idempotencyKeyStore = idempotencyKeyStore;
		this.properties = properties;
		this.apiErrorResponseWriter = apiErrorResponseWriter;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new IdempotencyInterceptor(idempotencyKeyStore, properties, apiErrorResponseWriter));
	}
}
