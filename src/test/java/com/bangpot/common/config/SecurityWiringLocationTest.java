package com.bangpot.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SecurityWiringLocationTest {

	@Test
	void keepsGlobalSecurityConfigInCommonPackage() {
		assertThatCode(() -> Class.forName("com.bangpot.common.config.SecurityConfig"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.infrastructure.security.SecurityConfig"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void keepsCommonTimeConfigurationOutOfAuthInfrastructure() {
		assertThatCode(() -> Class.forName("com.bangpot.common.config.TimeConfig"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.infrastructure.AuthConfiguration"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void keepsJwtFilterOutOfAuthPackage() {
		assertThatCode(() -> Class.forName("com.bangpot.common.security.JwtAuthenticationFilter"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.infrastructure.AuthSessionResolver"))
			.isInstanceOf(ClassNotFoundException.class);
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.infrastructure.security.JwtAuthenticationFilter"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void splitsAuthConfigurationIntoFocusedPropertyObjects() {
		assertThatCode(() -> Class.forName("com.bangpot.auth.infrastructure.config.AuthJwtProperties"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.bangpot.auth.infrastructure.config.AuthFrontendProperties"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.bangpot.auth.infrastructure.config.AuthRequiredTermsProperties"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.infrastructure.AuthProperties"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void keepsAuthApplicationExceptionsInDedicatedExceptionPackage() {
		assertThatCode(() -> Class.forName("com.bangpot.auth.application.exception.DuplicateNicknameException"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.application.DuplicateNicknameException"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void keepsAuthIntermediateApplicationTypesInDedicatedPackages() {
		assertThatCode(() -> Class.forName("com.bangpot.auth.application.usecase.CompleteTempUserUseCase$Command"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.bangpot.auth.application.usecase.LoginWithProviderUseCase$Result"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase$View"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.bangpot.auth.application.port.AuthUserRepository"))
			.doesNotThrowAnyException();
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.application.command.AuthCompletionCommand"))
			.isInstanceOf(ClassNotFoundException.class);
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.application.result.AuthLoginResult"))
			.isInstanceOf(ClassNotFoundException.class);
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.application.view.AuthMeView"))
			.isInstanceOf(ClassNotFoundException.class);
		assertThatThrownBy(() -> Class.forName("com.bangpot.auth.application.AuthUserRepository"))
			.isInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void doesNotKeepLegacySecurityJwtConfigurationBlock() throws IOException {
		String applicationYaml = Files.readString(Path.of(
			"C:/bangpot/backend/src/main/resources/application.yml"
		));

		assertThat(applicationYaml)
			.doesNotContain("security.jwt.")
			.doesNotContain("\nsecurity:\n  jwt:");
	}

	@Test
	void doesNotLeaveAllApiRoutesOpenByDefault() throws IOException {
		String securityConfig = Files.readString(Path.of(
			"C:/bangpot/backend/src/main/java/com/bangpot/common/config/SecurityConfig.java"
		));

		assertThat(securityConfig)
			.contains(".requestMatchers(\"/api/**\").authenticated()")
			.doesNotContain(".requestMatchers(\"/api/**\", \"/actuator/health\", \"/actuator/health/**\").permitAll()");
	}
}
