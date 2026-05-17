package com.banglog.auth.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AuthUseCaseStructureTest {

	@Test
	void exposesAuthApplicationBehaviorThroughUseCaseInterfacesAndServices() {
		assertThatCode(() -> Class.forName("com.banglog.auth.application.usecase.LoginWithProviderUseCase"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.banglog.auth.application.usecase.CompleteTempUserUseCase"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.banglog.auth.application.usecase.GetCurrentAuthUserUseCase"))
			.doesNotThrowAnyException();

		assertThatCode(() -> Class.forName("com.banglog.auth.application.service.LoginWithProviderService"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.banglog.auth.application.service.CompleteTempUserService"))
			.doesNotThrowAnyException();
		assertThatCode(() -> Class.forName("com.banglog.auth.application.service.GetCurrentAuthUserService"))
			.doesNotThrowAnyException();

		assertThatThrownBy(() -> Class.forName("com.banglog.auth.application.AuthService"))
			.isInstanceOf(ClassNotFoundException.class);
	}
}
