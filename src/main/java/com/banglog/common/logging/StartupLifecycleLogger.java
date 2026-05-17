package com.banglog.common.logging;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupLifecycleLogger {

	private static final String STARTUP_HEALTH_FAILED_MESSAGE =
		"\uC560\uD50C\uB9AC\uCF00\uC774\uC158 \uAE30\uB3D9 \uD6C4 \uB370\uC774\uD130\uBCA0\uC774\uC2A4 \uD655\uC778 \uC2E4\uD328";
	private static final String STARTUP_COMPLETED_MESSAGE =
		"\uC560\uD50C\uB9AC\uCF00\uC774\uC158 \uAE30\uB3D9 \uC644\uB8CC";

	private final Environment environment;
	private final ObjectProvider<DataSource> dataSourceProvider;

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		StartupDataSourceStatus dataSourceStatus = resolveDataSourceStatus();
		long startupMs = event.getTimeTaken() == null ? -1L : event.getTimeTaken().toMillis();

		if (dataSourceStatus.failed()) {
			log.error(
				"event=application.startup.health_failed message=\"{}\" context=application.startup.readiness profiles={} port={} startupMs={} exceptionType={}",
				STARTUP_HEALTH_FAILED_MESSAGE,
				resolveProfiles(),
				resolvePort(),
				startupMs,
				dataSourceStatus.exceptionType()
			);
			return;
		}

		log.info(
			"event=application.startup.completed message=\"{}\" context=application.startup profiles={} port={} startupMs={} datasource={}",
			STARTUP_COMPLETED_MESSAGE,
			resolveProfiles(),
			resolvePort(),
			startupMs,
			dataSourceStatus.description()
		);
	}

	private StartupDataSourceStatus resolveDataSourceStatus() {
		DataSource dataSource = dataSourceProvider.getIfAvailable();
		if (dataSource == null) {
			return StartupDataSourceStatus.success("none");
		}

		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			return StartupDataSourceStatus.success(
				metadata.getDatabaseProductName() + "-" + metadata.getDatabaseProductVersion()
			);
		} catch (Exception exception) {
			return StartupDataSourceStatus.failure(exception.getClass().getSimpleName());
		}
	}

	private String resolveProfiles() {
		String[] activeProfiles = environment.getActiveProfiles();
		if (activeProfiles.length == 0) {
			return "default";
		}
		return String.join(",", Arrays.asList(activeProfiles));
	}

	private String resolvePort() {
		String localPort = environment.getProperty("local.server.port");
		if (localPort != null && !localPort.isBlank()) {
			return localPort;
		}

		String serverPort = environment.getProperty("server.port");
		if (serverPort != null && !serverPort.isBlank()) {
			return serverPort;
		}

		return "8080";
	}

	private record StartupDataSourceStatus(boolean failed, String description, String exceptionType) {

		private static StartupDataSourceStatus success(String description) {
			return new StartupDataSourceStatus(false, description, null);
		}

		private static StartupDataSourceStatus failure(String exceptionType) {
			return new StartupDataSourceStatus(true, null, exceptionType);
		}
	}
}
