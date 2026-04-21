package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.PendingCrewReadRepository;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
import com.bangpot.user.domain.User;

class CancelMyPendingCrewJoinRequestServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void cancelsMyPendingCrewJoinRequestForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		pendingCrewReadRepository.putCancelable(
			7L,
			PendingCrewReadRepository.CancelResult.of(101L, 11L)
		);

		CancelMyPendingCrewJoinRequestUseCase.Result result = cancelMyPendingCrewJoinRequestUseCase.handle(
			CancelMyPendingCrewJoinRequestUseCase.Command.of(7L, 101L)
		);

		assertThat(result.joinRequestId()).isEqualTo(101L);
		assertThat(result.crewId()).isEqualTo(11L);
	}

	@Test
	void rejectsCancelForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> cancelMyPendingCrewJoinRequestUseCase.handle(
			CancelMyPendingCrewJoinRequestUseCase.Command.of(7L, 101L)
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCancelWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> cancelMyPendingCrewJoinRequestUseCase.handle(
			CancelMyPendingCrewJoinRequestUseCase.Command.of(7L, 101L)
		)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsCancelWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> cancelMyPendingCrewJoinRequestUseCase.handle(
			CancelMyPendingCrewJoinRequestUseCase.Command.of(77L, 101L)
		)).isInstanceOf(AuthUserNotFoundException.class);
	}
}
