package com.bangpot.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "banglog.auth.jwt")
public class AuthJwtProperties {

	private String issuer = "banglog";
	private String secret;
	private String cookieName = "access_token";
	private long accessTokenValiditySeconds = 604800;
	private boolean secureCookie;
}
