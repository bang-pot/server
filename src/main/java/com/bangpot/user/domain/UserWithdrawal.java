package com.bangpot.user.domain;

import java.time.Instant;

public class UserWithdrawal {

	private final Long userId;
	private final WithdrawalReasonCode reasonCode;
	private final String reasonDetail;
	private final Instant withdrawnAt;

	private UserWithdrawal(
		Long userId,
		WithdrawalReasonCode reasonCode,
		String reasonDetail,
		Instant withdrawnAt
	) {
		this.userId = userId;
		this.reasonCode = reasonCode;
		this.reasonDetail = reasonDetail;
		this.withdrawnAt = withdrawnAt;
	}

	public static UserWithdrawal create(
		Long userId,
		WithdrawalReasonCode reasonCode,
		String reasonDetail,
		Instant withdrawnAt
	) {
		return new UserWithdrawal(userId, reasonCode, reasonDetail, withdrawnAt);
	}

	public Long getUserId() {
		return userId;
	}

	public WithdrawalReasonCode getReasonCode() {
		return reasonCode;
	}

	public String getReasonDetail() {
		return reasonDetail;
	}

	public Instant getWithdrawnAt() {
		return withdrawnAt;
	}
}
