package com.bangpot.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "banglog.auth.frontend")
public class AuthFrontendProperties {

	private String baseUrl;
}
