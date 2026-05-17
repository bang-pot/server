package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.auth.domain.AuthUser;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.user.application.exception.WithdrawalNotAllowedException;
import com.banglog.user.application.usecase.WithdrawMyAccountUseCase;
import com.banglog.user.domain.User;
import com.banglog.user.domain.WithdrawalReasonCode;

class WithdrawMyAccountServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void withdrawsAccountWhenNoBlockingReasonExists() {
		AuthUser authUser = fullUser(7L, "banglog");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "banglog"));

		WithdrawMyAccountUseCase.Result result = withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.OTHER, null)
		);

		assertThat(result.withdrawnAt()).isEqualTo(BASE_TIME);
		assertThat(result.canLogin()).isFalse();
		assertThat(authUserRepository.findById(7L)).isEmpty();
		assertThat(userRepository.findById(7L)).isEmpty();
		assertThat(userWithdrawalRepository.findByUserId(7L)).isNotNull()
			.extracting(withdrawal -> withdrawal.getReasonCode().name(), withdrawal -> withdrawal.getReasonDetail())
			.containsExactly("OTHER", null);
	}

	@Test
	void storesOptionalReasonDetailWhenProvided() {
		AuthUser authUser = fullUser(7L, "banglog");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "banglog"));

		withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.SERVICE_UNSATISFIED, "Need a break")
		);

		assertThat(userWithdrawalRepository.findByUserId(7L)).isNotNull()
			.extracting(withdrawal -> withdrawal.getReasonCode().name(), withdrawal -> withdrawal.getReasonDetail())
			.containsExactly("SERVICE_UNSATISFIED", "Need a break");
	}

	@Test
	void rejectsWithdrawalWhenBlockingReasonExists() {
		AuthUser authUser = fullUser(7L, "banglog");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "banglog"));
		Crew crew = Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null);
		crew.assignId(31L);
		crewRepository.save(crew);
		crewMemberRepository.save(CrewMember.createMember(31L, 7L));

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
		authUserRepository.save(fullUser(7L, "banglog"));

		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsWithdrawalWhenAuthUserDoesNotExist() {
		userRepository.save(User.create(7L, "banglog"));

		assertThatThrownBy(() -> withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(7L, WithdrawalReasonCode.NOT_USING, null)
		))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("완료된 회원의 인증 정보가 없습니다.");
	}
}
