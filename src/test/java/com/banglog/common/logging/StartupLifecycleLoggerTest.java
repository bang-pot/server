package com.banglog.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Duration;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class StartupLifecycleLoggerTest {

	@Test
	void logsStartupCompletedWhenDatasourceCheckSucceeds() throws SQLException {
		MockEnvironment environment = new MockEnvironment()
			.withProperty("server.port", "8080");
		StartupLifecycleLogger logger = new StartupLifecycleLogger(environment, objectProvider(null));
		ListAppender<ILoggingEvent> appender = attachAppender();
		ApplicationReadyEvent event = readyEvent(Duration.ofSeconds(2));

		logger.onApplicationReady(event);

		assertThat(appender.list).hasSize(1);
		assertThat(appender.list.getFirst().getLevel().toString()).isEqualTo("INFO");
		assertThat(appender.list.getFirst().getFormattedMessage())
			.contains("event=application.startup.completed")
			.contains("port=8080")
			.contains("startupMs=2000")
			.contains("datasource=none");
	}

	@Test
	void logsStartupHealthFailureWithoutSensitiveMessageWhenDatasourceCheckFails() throws SQLException {
		Environment environment = new MockEnvironment()
			.withProperty("server.port", "8080");
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenThrow(new SQLException("password=secret"));
		StartupLifecycleLogger logger = new StartupLifecycleLogger(environment, objectProvider(dataSource));
		ListAppender<ILoggingEvent> appender = attachAppender();
		ApplicationReadyEvent event = readyEvent(Duration.ofMillis(500));

		logger.onApplicationReady(event);

		assertThat(appender.list).hasSize(1);
		assertThat(appender.list.getFirst().getLevel().toString()).isEqualTo("ERROR");
		assertThat(appender.list.getFirst().getFormattedMessage())
			.contains("event=application.startup.health_failed")
			.contains("exceptionType=SQLException")
			.doesNotContain("password=secret");
	}

	private ListAppender<ILoggingEvent> attachAppender() {
		Logger logger = (Logger)LoggerFactory.getLogger(StartupLifecycleLogger.class);
		logger.detachAndStopAllAppenders();
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		return appender;
	}

	@SuppressWarnings("unchecked")
	private ObjectProvider<DataSource> objectProvider(DataSource dataSource) {
		ObjectProvider<DataSource> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(dataSource);
		return provider;
	}

	private ApplicationReadyEvent readyEvent(Duration duration) {
		ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
		when(event.getTimeTaken()).thenReturn(duration);
		when(event.getSpringApplication()).thenReturn(new SpringApplication());
		return event;
	}
}
