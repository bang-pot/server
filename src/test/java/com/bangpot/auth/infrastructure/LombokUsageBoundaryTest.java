package com.bangpot.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LombokUsageBoundaryTest {

	@Test
	void usesGetterAndSetterForInfrastructurePropertyObjects() throws IOException {
		assertUsesGetterSetter(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/infrastructure/config/AuthJwtProperties.java"
		);
		assertUsesGetterSetter(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/infrastructure/config/AuthFrontendProperties.java"
		);
		assertUsesGetterSetter(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/infrastructure/config/AuthRequiredTermsProperties.java"
		);
	}

	@Test
	void usesRequiredArgsConstructorForSimpleDependencyInjectionClasses() throws IOException {
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/application/service/CheckNicknameAvailabilityService.java"
		);
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/application/service/LoginWithProviderService.java"
		);
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/application/service/CompleteTempUserService.java"
		);
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/application/service/GetCurrentAuthUserService.java"
		);
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/infrastructure/oauth/KakaoOAuth2AuthenticationSuccessHandler.java"
		);
		assertUsesRequiredArgsConstructor(
			"C:/bangpot/backend/src/main/java/com/bangpot/auth/infrastructure/oauth/KakaoOAuth2AuthenticationFailureHandler.java"
		);
	}

	private void assertUsesGetterSetter(String path) throws IOException {
		String source = readSource(path);
		assertThat(source).contains("@Getter").contains("@Setter");
		assertThat(source)
			.doesNotContain("public String get")
			.doesNotContain("public void set")
			.doesNotContain("public long get")
			.doesNotContain("public boolean is");
	}

	private void assertUsesRequiredArgsConstructor(String path) throws IOException {
		String source = readSource(path);
		assertThat(source).contains("@RequiredArgsConstructor");
	}

	private String readSource(String path) throws IOException {
		return Files.readString(Path.of(path));
	}
}
