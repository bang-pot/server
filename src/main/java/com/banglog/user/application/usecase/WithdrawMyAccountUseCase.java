package com.banglog.user.application.usecase;

import java.time.Instant;

import com.banglog.user.domain.WithdrawalReasonCode;

public interface WithdrawMyAccountUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		WithdrawalReasonCode reasonCode,
		String reasonDetail
	) {
		public static Command of(Long userId, WithdrawalReasonCode reasonCode, String reasonDetail) {
			return new Command(userId, reasonCode, reasonDetail);
		}
	}

	record Result(
		Instant withdrawnAt,
		boolean canLogin
	) {
		public static Result of(Instant withdrawnAt, boolean canLogin) {
			return new Result(withdrawnAt, canLogin);
		}
	}
}
