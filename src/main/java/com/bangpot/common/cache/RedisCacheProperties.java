package com.bangpot.common.cache;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bangpot.cache.redis")
public record RedisCacheProperties(
	boolean enabled,
	Duration defaultTtl,
	Duration exploreThemeSearchTtl,
	Duration exploreFiltersTtl,
	Duration homeAnonymousTtl
) {

	public RedisCacheProperties {
		if (defaultTtl == null) {
			defaultTtl = Duration.ofMinutes(5);
		}
		if (exploreThemeSearchTtl == null) {
			exploreThemeSearchTtl = Duration.ofSeconds(60);
		}
		if (exploreFiltersTtl == null) {
			exploreFiltersTtl = Duration.ofHours(6);
		}
		if (homeAnonymousTtl == null) {
			homeAnonymousTtl = Duration.ofSeconds(60);
		}
	}
}
