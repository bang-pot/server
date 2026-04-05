package com.bangpot.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;

@ExtendWith(OutputCaptureExtension.class)
class RequestTracingFilterTest {

	private final RequestTracingFilter filter = new RequestTracingFilter();

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void reusesIncomingRequestIdAndWritesCompletionLog(CapturedOutput output)
		throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		request.addHeader("X-Request-Id", "req-health-1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		filter.doFilter(request, response, filterChain);

		assertThat(response.getHeader("X-Request-Id")).isEqualTo("req-health-1");
		assertThat(output.getOut())
			.contains("event=request.completed")
			.contains("message=\"요청 처리 완료\"")
			.contains("requestId=req-health-1")
			.contains("method=GET")
			.contains("path=/api/auth/me");
	}

	@Test
	void generatesRequestIdWhenHeaderIsMissing(CapturedOutput output)
		throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getHeader("X-Request-Id")).isNotBlank();
		assertThat(output.getOut())
			.contains("event=request.completed")
			.contains("message=\"요청 처리 완료\"");
	}
}
