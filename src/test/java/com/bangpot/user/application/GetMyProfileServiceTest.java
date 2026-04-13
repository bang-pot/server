package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.User;

class GetMyProfileServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyProfileForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot", true));

		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L));

		assertThat(result.id()).isEqualTo(7L);
		assertThat(result.nickname()).isEqualTo("bangpot");
	}

	@Test
	void rejectsMyProfileLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMyProfileLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsProfileLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(77L)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
