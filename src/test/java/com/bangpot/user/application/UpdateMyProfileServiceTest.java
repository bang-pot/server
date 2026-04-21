package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.User;

class UpdateMyProfileServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void updatesNicknameForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			UpdateMyProfileUseCase.Command.of(7L, "  new-pot  ")
		);

		assertThat(result.id()).isEqualTo(7L);
		assertThat(result.nickname()).isEqualTo("new-pot");
		assertThat(userRepository.findById(7L)).get().extracting(User::getNickname).isEqualTo("new-pot");
	}

	@Test
	void allowsKeepingSameNicknameWithoutDuplicateFailure() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		userRepository.save(User.create(8L, "other"));

		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			UpdateMyProfileUseCase.Command.of(7L, "bangpot")
		);

		assertThat(result.nickname()).isEqualTo("bangpot");
	}

	@Test
	void rejectsNicknameUpdateForBlankNickname() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		assertThatThrownBy(() -> updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(7L, "   ")))
			.isInstanceOf(InvalidNicknameException.class);
	}

	@Test
	void rejectsNicknameUpdateWhenNicknameAlreadyExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		userRepository.save(User.create(8L, "taken"));

		assertThatThrownBy(() -> updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(7L, "taken")))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	@Test
	void rejectsNicknameUpdateForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(7L, "new-pot")))
			.isInstanceOf(AccessDeniedException.class);
	}
}
