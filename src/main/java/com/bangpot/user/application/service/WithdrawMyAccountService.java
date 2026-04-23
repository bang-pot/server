package com.bangpot.user.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.user.application.exception.WithdrawalNotAllowedException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.port.UserWithdrawalRepository;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.domain.UserWithdrawal;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawMyAccountService implements WithdrawMyAccountUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final UserWithdrawalRepository userWithdrawalRepository;

	private final GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;
	private final Clock clock;

	@Override
	public Result handle(Command command) {
		userRepository.findById(command.userId())
			.orElseThrow(() -> new AccessDeniedException("프로필 완료가 필요합니다."));

		MyWithdrawalCheckView withdrawalCheck = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(command.userId())
		);
		if (!withdrawalCheck.canWithdraw()) {
			throw new WithdrawalNotAllowedException();
		}
		if (authUserRepository.findById(command.userId()).isEmpty()) {
			throw new IllegalStateException(
				"완료된 회원의 인증 정보가 없습니다. userId=" + command.userId()
			);
		}

		Instant withdrawnAt = Instant.now(clock);
		userWithdrawalRepository.save(
			UserWithdrawal.create(command.userId(), command.reasonCode(), command.reasonDetail(), withdrawnAt)
		);
		authUserRepository.deleteById(command.userId());
		userRepository.withdrawById(command.userId(), tombstoneNickname(command.userId()), withdrawnAt);

		return Result.of(withdrawnAt, false);
	}

	private String tombstoneNickname(Long userId) {
		return "withdrawn-user-" + userId;
	}
}
