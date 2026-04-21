package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.exception.WithdrawalNotAllowedException;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.WithdrawalReasonCode;

class WithdrawMyAccountServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void withdrawsAccountWhenNoBlockingReasonExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		WithdrawMyAccountUseCase.Result result = withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.OTHER, null)
		);

		assertThat(result.withdrawnAt()).isEqualTo(BASE_TIME.toString());
		assertThat(result.canLogin()).isFalse();
		assertThat(authUserRepository.findById(7L)).isEmpty();
		assertThat(userRepository.findById(7L)).isEmpty();
		assertThat(userWithdrawalRepository.findByUserId(7L)).isNotNull()
			.extracting(withdrawal -> withdrawal.getReasonCode().name(), withdrawal -> withdrawal.getReasonDetail())
			.containsExactly("OTHER", null);
	}

	@Test
	void storesOptionalReasonDetailWhenProvided() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.SERVICE_UNSATISFIED, "Need a break")
		);

		assertThat(userWithdrawalRepository.findByUserId(7L)).isNotNull()
			.extracting(withdrawal -> withdrawal.getReasonCode().name(), withdrawal -> withdrawal.getReasonDetail())
			.containsExactly("SERVICE_UNSATISFIED", "Need a break");
	}

	@Test
	void rejectsWithdrawalWhenBlockingReasonExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		withdrawalCheckReadRepository.putResult(
			7L,
			com.bangpot.user.application.port.WithdrawalCheckReadRepository.View.of(
				List.of(
					com.bangpot.user.application.port.WithdrawalCheckReadRepository.ActiveCrew.of(31L, "Alpha Crew")
				),
				List.of()
			)
		);

		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(WithdrawalNotAllowedException.class);
		assertThat(authUserRepository.findById(7L)).isPresent();
		assertThat(userRepository.findById(7L)).isPresent();
		assertThat(userWithdrawalRepository.findByUserId(7L)).isNull();
	}

	@Test
	void rejectsWithdrawalForTempUser() {
		authUserRepository.save(tempUser(7L));

		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsWithdrawalWhenUserRowIsMissing() {
		authUserRepository.save(fullUser(7L, "bangpot"));

		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsWithdrawalWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
