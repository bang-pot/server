package com.banglog.common.cache;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching(proxyTargetClass = true)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheConfig {

	@Bean
	@ConditionalOnProperty(prefix = "banglog.cache.redis", name = "enabled", havingValue = "true")
	CacheManager redisCacheManager(
		RedisConnectionFactory redisConnectionFactory,
		RedisCacheProperties properties
	) {
		RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(properties.defaultTtl())
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
			);

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(defaultConfiguration)
			.withInitialCacheConfigurations(Map.of(
				CacheNames.EXPLORE_THEME_SEARCH,
				defaultConfiguration.entryTtl(properties.exploreThemeSearchTtl()),
				CacheNames.EXPLORE_FILTERS,
				defaultConfiguration.entryTtl(properties.exploreFiltersTtl()),
				CacheNames.HOME_ANONYMOUS,
				defaultConfiguration.entryTtl(properties.homeAnonymousTtl())
			))
			.build();
	}

	@Bean
	@ConditionalOnProperty(
		prefix = "banglog.cache.redis",
		name = "enabled",
		havingValue = "false",
		matchIfMissing = true
	)
	CacheManager noOpCacheManager() {
		return new NoOpCacheManager();
	}
}
