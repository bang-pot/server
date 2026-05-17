package com.banglog.auth.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "banglog.auth")
public class AuthRequiredTermsProperties {

	private String requiredTermsVersion = "2026-03-25";
}
