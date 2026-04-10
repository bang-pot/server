package com.bangpot.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AuthObjectCreationEncapsulationTest {

	@Test
	void avoidsDirectIntermediateDtoInstantiationInControllerAndService() throws IOException {
		String authControllerSource = readSource(
			"src/main/java/com/bangpot/auth/presentation/AuthController.java"
		);
		String loginWithProviderServiceSource = readSource(
			"src/main/java/com/bangpot/auth/application/service/LoginWithProviderService.java"
		);
		String completeTempUserServiceSource = readSource(
			"src/main/java/com/bangpot/auth/application/service/CompleteTempUserService.java"
		);
		String checkNicknameAvailabilityServiceSource = readSource(
			"src/main/java/com/bangpot/user/application/service/CheckNicknameAvailabilityService.java"
		);
		String getCurrentAuthUserServiceSource = readSource(
			"src/main/java/com/bangpot/auth/application/service/GetCurrentAuthUserService.java"
		);

		assertThat(authControllerSource)
			.doesNotContain("new CompleteTempUserUseCase.Command(")
			.doesNotContain("new AuthCompletionResponse(")
			.doesNotContain("resolveAuthenticatedUserId(")
			.contains("me(Authentication authentication)")
			.contains("complete(");
		assertThat(loginWithProviderServiceSource).doesNotContain("new Result(");
		assertThat(completeTempUserServiceSource).doesNotContain("new Result(");
		assertThat(checkNicknameAvailabilityServiceSource).doesNotContain("new Result(");
		assertThat(getCurrentAuthUserServiceSource)
			.doesNotContain("new View(")
			.doesNotContain("new AuthenticatedUserView(");
	}

	private String readSource(String relativePath) throws IOException {
		return Files.readString(Path.of(relativePath));
	}
}
