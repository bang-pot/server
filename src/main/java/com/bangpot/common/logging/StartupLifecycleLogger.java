package com.bangpot.common.logging;

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

	private final Environment environment;
	private final ObjectProvider<DataSource> dataSourceProvider;

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		StartupDataSourceStatus dataSourceStatus = resolveDataSourceStatus();
		long startupMs = event.getTimeTaken() == null ? -1L : event.getTimeTaken().toMillis();

		if (dataSourceStatus.failed()) {
			log.error(
				"event=application.startup.health_failed message=\"애플리케이션 기동 후 데이터베이스 확인 실패\" context=application.startup.readiness profiles={} port={} startupMs={} exceptionType={}",
				resolveProfiles(),
				resolvePort(),
				startupMs,
				dataSourceStatus.exceptionType()
			);
			return;
		}

		log.info(
			"event=application.startup.completed message=\"애플리케이션 기동 완료\" context=application.startup profiles={} port={} startupMs={} datasource={}",
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
