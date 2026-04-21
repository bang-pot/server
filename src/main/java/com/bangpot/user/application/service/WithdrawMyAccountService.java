package com.bangpot.user.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.port.UserWithdrawalRepository;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.application.exception.WithdrawalNotAllowedException;
import com.bangpot.user.domain.UserWithdrawal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawMyAccountService implements WithdrawMyAccountUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;
	private final UserWithdrawalRepository userWithdrawalRepository;
	private final Clock clock;

	@Override
	public Result handle(Command command) {
		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.isTemp()) {
			throw new AccessDeniedException("가입 완료 사용자만 이용할 수 있습니다.");
		}
		userRepository.findById(command.userId())
			.orElseThrow(() -> new UserNotFoundException(command.userId()));

		GetMyWithdrawalCheckUseCase.Result withdrawalCheck = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(command.userId())
		);
		if (!withdrawalCheck.canWithdraw()) {
			throw new WithdrawalNotAllowedException();
		}

		Instant withdrawnAt = Instant.now(clock);
		userWithdrawalRepository.save(
			UserWithdrawal.create(command.userId(), command.reasonCode(), command.reasonDetail(), withdrawnAt)
		);
		authUser.withdraw(tombstoneProviderId(command.userId(), withdrawnAt), withdrawnAt);
		authUserRepository.save(authUser);
		userRepository.withdrawById(command.userId(), tombstoneNickname(command.userId()), withdrawnAt);

		return Result.of(withdrawnAt.toString(), false);
	}

	private String tombstoneProviderId(Long userId, Instant withdrawnAt) {
		return "withdrawn:" + userId + ":" + withdrawnAt.toEpochMilli();
	}

	private String tombstoneNickname(Long userId) {
		return "withdrawn-user-" + userId;
	}
}
