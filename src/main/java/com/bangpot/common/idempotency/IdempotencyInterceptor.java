package com.bangpot.common.idempotency;

import java.io.IOException;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.bangpot.common.error.ApiErrorResponseWriter;
import com.bangpot.common.error.CommonErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

	private static final String KEY_PREFIX = "idempotency:v1";

	private final IdempotencyKeyStore idempotencyKeyStore;
	private final IdempotencyProperties properties;
	private final ApiErrorResponseWriter apiErrorResponseWriter;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws IOException {
		if (!properties.enabled() || !(handler instanceof HandlerMethod handlerMethod)) {
			return true;
		}

		Idempotent idempotent = findIdempotent(handlerMethod);
		if (idempotent == null) {
			return true;
		}

		String idempotencyKey = request.getHeader(properties.headerName());
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			if (idempotent.required()) {
				apiErrorResponseWriter.write(response, CommonErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED);
				return false;
			}
			return true;
		}

		if (!idempotencyKeyStore.reserve(buildStorageKey(request, idempotencyKey), properties.ttl())) {
			apiErrorResponseWriter.write(response, CommonErrorCode.COMMON_DUPLICATE_REQUEST);
			return false;
		}
		return true;
	}

	private Idempotent findIdempotent(HandlerMethod handlerMethod) {
		Idempotent methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(
			handlerMethod.getMethod(),
			Idempotent.class
		);
		if (methodAnnotation != null) {
			return methodAnnotation;
		}
		return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Idempotent.class);
	}

	private String buildStorageKey(HttpServletRequest request, String idempotencyKey) {
		return KEY_PREFIX
			+ ":user=" + resolveUserId(request)
			+ ":method=" + request.getMethod()
			+ ":path=" + request.getRequestURI()
			+ ":key=" + idempotencyKey.trim();
	}

	private String resolveUserId(HttpServletRequest request) {
		if (request.getUserPrincipal() instanceof Authentication authentication
			&& authentication.getPrincipal() instanceof Long userId) {
			return String.valueOf(userId);
		}
		if (request.getUserPrincipal() != null) {
			return request.getUserPrincipal().getName();
		}
		return "anonymous";
	}
}
