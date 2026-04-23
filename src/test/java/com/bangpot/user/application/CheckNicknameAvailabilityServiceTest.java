package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.domain.User;

class CheckNicknameAvailabilityServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsAvailableWhenNormalizedNicknameDoesNotExist() {
		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("  bangpot  ")
		);

		assertThat(result.nickname()).isEqualTo("bangpot");
		assertThat(result.available()).isTrue();
	}

	@Test
	void returnsUnavailableWhenNicknameAlreadyExists() {
		userRepository.save(User.create(1L, "bangpot"));

		CheckNicknameAvailabilityUseCase.Result result = checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("bangpot")
		);

		assertThat(result.nickname()).isEqualTo("bangpot");
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
