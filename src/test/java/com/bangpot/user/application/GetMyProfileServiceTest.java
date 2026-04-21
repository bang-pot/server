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
		userRepository.save(User.rehydrate(7L, "bangpot"));
		profileHubReadRepository.putCounts(7L, 5L, 3L, 2L, 1L);

		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L));

		assertThat(result.id()).isEqualTo(7L);
		assertThat(result.nickname()).isEqualTo("bangpot");
		assertThat(result.profileImageUrl()).isNull();
		assertThat(result.createdMeetingsCount()).isEqualTo(5L);
		assertThat(result.joinedMeetingsCount()).isEqualTo(3L);
		assertThat(result.myCrewsCount()).isEqualTo(2L);
		assertThat(result.pendingCrewsCount()).isEqualTo(1L);
	}

	@Test
	void defaultsHubCountsToZeroWhenNoActivityExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));

		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L));

		assertThat(result.createdMeetingsCount()).isZero();
		assertThat(result.joinedMeetingsCount()).isZero();
		assertThat(result.myCrewsCount()).isZero();
		assertThat(result.pendingCrewsCount()).isZero();
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
