package com.banglog.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.banglog.common.error.ApiErrorResponseFactory;
import com.banglog.common.error.ApiErrorResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

class IdempotencyInterceptorTest {

	private final FakeIdempotencyKeyStore idempotencyKeyStore = new FakeIdempotencyKeyStore();
	private final IdempotencyProperties properties = new IdempotencyProperties(
		true,
		"Idempotency-Key",
		Duration.ofSeconds(30)
	);
	private final IdempotencyInterceptor interceptor = new IdempotencyInterceptor(
		idempotencyKeyStore,
		properties,
		new ApiErrorResponseWriter(new ObjectMapper(), new ApiErrorResponseFactory())
	);

	@Test
	void blocksRequestWhenRequiredIdempotencyHeaderIsNotProvidedByDefault() throws Exception {
		MockHttpServletRequest request = request();
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = interceptor.preHandle(request, response, handlerMethod("idempotentCommand"));

		assertThat(result).isFalse();
		assertThat(response.getStatus()).isEqualTo(400);
		assertThat(response.getContentAsString(StandardCharsets.UTF_8))
			.contains("COMMON_IDEMPOTENCY_KEY_REQUIRED")
			.contains("Idempotency-Key 헤더가 필요합니다.");
		assertThat(idempotencyKeyStore.reservedKey).isNull();
	}

	@Test
	void passesRequestWhenOptionalIdempotencyHeaderIsNotProvided() throws Exception {
		MockHttpServletRequest request = request();
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = interceptor.preHandle(request, response, handlerMethod("optionalIdempotentCommand"));

		assertThat(result).isTrue();
		assertThat(idempotencyKeyStore.reservedKey).isNull();
	}

	@Test
	void reservesIdempotencyKeyWhenHeaderIsProvided() throws Exception {
		MockHttpServletRequest request = request();
		request.addHeader("Idempotency-Key", "request-1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = interceptor.preHandle(request, response, handlerMethod("idempotentCommand"));

		assertThat(result).isTrue();
		assertThat(idempotencyKeyStore.reservedKey)
			.isEqualTo("idempotency:v1:user=77:method=POST:path=/api/crews/1/meetings/10/join:key=request-1");
		assertThat(idempotencyKeyStore.reservedTtl).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	void blocksDuplicateRequestWhenReserveFails() throws Exception {
		idempotencyKeyStore.reserveResult = false;
		MockHttpServletRequest request = request();
		request.addHeader("Idempotency-Key", "request-1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = interceptor.preHandle(request, response, handlerMethod("idempotentCommand"));

		assertThat(result).isFalse();
		assertThat(response.getStatus()).isEqualTo(409);
		assertThat(response.getContentAsString(StandardCharsets.UTF_8))
			.contains("COMMON_DUPLICATE_REQUEST")
			.contains("이미 처리 중인 요청입니다.");
	}

	@Test
	void ignoresNonIdempotentHandler() throws Exception {
		MockHttpServletRequest request = request();
		request.addHeader("Idempotency-Key", "request-1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = interceptor.preHandle(request, response, handlerMethod("plainCommand"));

		assertThat(result).isTrue();
		assertThat(idempotencyKeyStore.reservedKey).isNull();
	}

	private MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/crews/1/meetings/10/join");
		request.setUserPrincipal(() -> "77");
		return request;
	}

	private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
		Method method = TestController.class.getDeclaredMethod(methodName);
		return new HandlerMethod(new TestController(), method);
	}

	private static final class FakeIdempotencyKeyStore implements IdempotencyKeyStore {

		private boolean reserveResult = true;
		private String reservedKey;
		private Duration reservedTtl;

		@Override
		public boolean reserve(String key, Duration ttl) {
			reservedKey = key;
			reservedTtl = ttl;
			return reserveResult;
		}
	}

	private static final class TestController {

		@Idempotent
		void idempotentCommand() {
		}

		@Idempotent(required = false)
		void optionalIdempotentCommand() {
		}

		void plainCommand() {
		}
	}
}
