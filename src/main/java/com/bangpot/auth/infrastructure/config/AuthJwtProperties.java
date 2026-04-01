package com.bangpot.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bangpot.auth.jwt")
public class AuthJwtProperties {

	private String issuer = "bangpot";
	private String secret = "local-local-local-local-local-local-secret-12345";
	private String cookieName = "BANGPOT_ACCESS_TOKEN";
	private long accessTokenValiditySeconds = 604800;
	private boolean secureCookie;
}
