package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.WithdrawalCheckReadRepository;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.domain.User;

class GetMyWithdrawalCheckServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsWithdrawalCheckForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		withdrawalCheckReadRepository.putResult(
			7L,
			WithdrawalCheckReadRepository.View.of(
				List.of(
					WithdrawalCheckReadRepository.ActiveCrew.of(31L, "Alpha Crew")
				),
				List.of(
					WithdrawalCheckReadRepository.ParticipatingMeeting.of(
						101L,
						"Friday Escape",
						31L,
						"Alpha Crew",
						"RECRUITING",
						"2026-04-20",
						"19:00",
						"HOST"
					)
				)
			)
		);

		GetMyWithdrawalCheckUseCase.Result result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isFalse();
		assertThat(result.blockingActiveCrews()).hasSize(1);
		assertThat(result.blockingActiveCrews().get(0).crewId()).isEqualTo(31L);
		assertThat(result.blockingActiveCrews().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.blockingParticipatingMeetings()).hasSize(1);
		assertThat(result.blockingParticipatingMeetings().get(0).meetingId()).isEqualTo(101L);
		assertThat(result.blockingParticipatingMeetings().get(0).meetingTitle()).isEqualTo("Friday Escape");
		assertThat(result.blockingParticipatingMeetings().get(0).meetingStatus()).isEqualTo("RECRUITING");
		assertThat(result.blockingParticipatingMeetings().get(0).participationRole()).isEqualTo("HOST");
	}

	@Test
	void allowsWithdrawalWhenNoBlockingReasonExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		GetMyWithdrawalCheckUseCase.Result result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isTrue();
		assertThat(result.blockingActiveCrews()).isEmpty();
		assertThat(result.blockingParticipatingMeetings()).isEmpty();
	}

	@Test
	void rejectsWithdrawalCheckForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsWithdrawalCheckWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(7L)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsWithdrawalCheckWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(77L)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
