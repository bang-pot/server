package com.banglog.common.idempotency;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "banglog.idempotency")
public record IdempotencyProperties(
	boolean enabled,
	String headerName,
	Duration ttl
) {

	public IdempotencyProperties {
		if (headerName == null || headerName.isBlank()) {
			headerName = "Idempotency-Key";
		}
		if (ttl == null) {
			ttl = Duration.ofSeconds(30);
		}
	}
}
