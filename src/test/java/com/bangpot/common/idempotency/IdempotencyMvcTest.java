package com.bangpot.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.ApiErrorResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

class IdempotencyMvcTest {

	private FakeIdempotencyKeyStore idempotencyKeyStore;
	private TestController controller;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		idempotencyKeyStore = new FakeIdempotencyKeyStore();
		controller = new TestController();
		IdempotencyInterceptor interceptor = new IdempotencyInterceptor(
			idempotencyKeyStore,
			new IdempotencyProperties(true, "Idempotency-Key", Duration.ofSeconds(30)),
			new ApiErrorResponseWriter(new ObjectMapper(), new ApiErrorResponseFactory())
		);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.addInterceptors(interceptor)
			.build();
	}

	@Test
	void blocksAnnotatedEndpointWhenRequiredHeaderIsMissing() throws Exception {
		mockMvc.perform(post("/commands/idempotent").principal(() -> "77"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_IDEMPOTENCY_KEY_REQUIRED"))
			.andExpect(jsonPath("$.message").value("Idempotency-Key 헤더가 필요합니다."));

		assertThat(controller.idempotentCallCount).isZero();
		assertThat(idempotencyKeyStore.reservedKey).isNull();
	}

	@Test
	void proceedsAnnotatedEndpointWhenIdempotencyKeyIsReserved() throws Exception {
		mockMvc.perform(post("/commands/idempotent")
				.principal(() -> "77")
				.header("Idempotency-Key", "request-1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("ok"));

		assertThat(controller.idempotentCallCount).isOne();
		assertThat(idempotencyKeyStore.reservedKey)
			.isEqualTo("idempotency:v1:user=77:method=POST:path=/commands/idempotent:key=request-1");
		assertThat(idempotencyKeyStore.reservedTtl).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	void blocksAnnotatedEndpointWhenIdempotencyKeyAlreadyExists() throws Exception {
		idempotencyKeyStore.reserveResult = false;

		mockMvc.perform(post("/commands/idempotent")
				.principal(() -> "77")
				.header("Idempotency-Key", "request-1"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("COMMON_DUPLICATE_REQUEST"))
			.andExpect(jsonPath("$.message").value("이미 처리 중인 요청입니다."));

		assertThat(controller.idempotentCallCount).isZero();
	}

	@Test
	void proceedsOptionalAnnotatedEndpointWhenHeaderIsMissing() throws Exception {
		mockMvc.perform(post("/commands/optional").principal(() -> "77"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("optional"));

		assertThat(controller.optionalCallCount).isOne();
		assertThat(idempotencyKeyStore.reservedKey).isNull();
	}

	@Test
	void ignoresEndpointWithoutIdempotentAnnotation() throws Exception {
		mockMvc.perform(post("/commands/plain").principal(() -> "77"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("plain"));

		assertThat(controller.plainCallCount).isOne();
		assertThat(idempotencyKeyStore.reservedKey).isNull();
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

	@RestController
	private static final class TestController {

		private int idempotentCallCount;
		private int optionalCallCount;
		private int plainCallCount;

		@Idempotent
		@PostMapping("/commands/idempotent")
		Response idempotentCommand() {
			idempotentCallCount++;
			return new Response("ok");
		}

		@Idempotent(required = false)
		@PostMapping("/commands/optional")
		Response optionalCommand() {
			optionalCallCount++;
			return new Response("optional");
		}

		@PostMapping("/commands/plain")
		Response plainCommand() {
			plainCallCount++;
			return new Response("plain");
		}
	}

	private record Response(String result) {
	}
}
