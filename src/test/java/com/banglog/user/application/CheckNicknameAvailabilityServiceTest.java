package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.banglog.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.banglog.user.domain.User;

class CheckNicknameAvailabilityServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsAvailableWhenNormalizedNicknameDoesNotExist() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("  banglog  ")
		);

		assertThat(result.nickname()).isEqualTo("banglog");
		assertThat(result.available()).isTrue();
	}

	@Test
	void returnsUnavailableWhenNicknameAlreadyExists() {
		userRepository.save(User.create(1L, "banglog"));

		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("banglog")
		);

		assertThat(result.nickname()).isEqualTo("banglog");
		assertThat(result.available()).isFalse();
	}

	@Test
	void returnsInvalidWhenNicknameIsBlank() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("   ")
		);

		assertThat(result.nickname()).isNull();
		assertThat(result.available()).isFalse();
	}

	@Test
	void returnsInvalidWhenNicknameIsTooShort() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("a")
		);

		assertThat(result.nickname()).isNull();
		assertThat(result.available()).isFalse();
	}

	@Test
	void returnsInvalidWhenNicknameIsTooLong() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("abcdefghijklmn")
		);

		assertThat(result.nickname()).isNull();
		assertThat(result.available()).isFalse();
	}

	@Test
	void returnsInvalidWhenNicknameContainsDisallowedCharacters() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("bang-pot")
		);

		assertThat(result.nickname()).isNull();
		assertThat(result.available()).isFalse();
	}
}
