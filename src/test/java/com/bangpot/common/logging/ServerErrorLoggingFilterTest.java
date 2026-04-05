package com.bangpot.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;

class ServerErrorLoggingFilterTest {

	@Test
	void logsErrorWhenUnhandledExceptionBubblesUp() {
		ServerErrorLoggingFilter filter = new ServerErrorLoggingFilter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/fail");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ListAppender<ILoggingEvent> appender = attachAppender();

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter.doFilter(
			request,
			response,
			throwingChain(new IllegalStateException("boom"))
		)).isInstanceOf(IllegalStateException.class);

		assertThat(appender.list).hasSize(1);
		assertThat(appender.list.getFirst().getLevel().toString()).isEqualTo("ERROR");
		assertThat(appender.list.getFirst().getFormattedMessage())
			.contains("event=request.failed")
			.contains("path=/api/fail")
			.contains("status=500")
			.contains("exceptionType=IllegalStateException");
	}

	@Test
	void logsErrorWhenResponseStatusIsFiveHundredWithoutException() throws Exception {
		ServerErrorLoggingFilter filter = new ServerErrorLoggingFilter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/fail");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ListAppender<ILoggingEvent> appender = attachAppender();

		filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse)res).setStatus(503));

		assertThat(appender.list).hasSize(1);
		assertThat(appender.list.getFirst().getFormattedMessage())
			.contains("event=request.failed")
			.contains("path=/api/fail")
			.contains("status=503")
			.contains("exceptionType=HttpStatus503");
	}

	@Test
	void skipsHealthEndpointFailures() throws Exception {
		ServerErrorLoggingFilter filter = new ServerErrorLoggingFilter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health/readiness");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ListAppender<ILoggingEvent> appender = attachAppender();

		filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse)res).setStatus(503));

		assertThat(appender.list).isEmpty();
	}

	private ListAppender<ILoggingEvent> attachAppender() {
		Logger logger = (Logger)LoggerFactory.getLogger(ServerErrorLoggingFilter.class);
		logger.detachAndStopAllAppenders();
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		return appender;
	}

	private FilterChain throwingChain(RuntimeException exception) {
		return (request, response) -> {
			throw exception;
		};
	}
}
