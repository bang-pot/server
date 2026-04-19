package com.bangpot.user.application.usecase;

import com.bangpot.user.domain.WithdrawalReasonCode;

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
		String withdrawnAt,
		boolean canLogin
	) {
		public static Result of(String withdrawnAt, boolean canLogin) {
			return new Result(withdrawnAt, canLogin);
		}
	}
}
