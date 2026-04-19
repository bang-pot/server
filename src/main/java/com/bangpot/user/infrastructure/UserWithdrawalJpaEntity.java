package com.bangpot.user.infrastructure;

import java.time.Instant;

import com.bangpot.user.domain.WithdrawalReasonCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "user_withdrawals")
class UserWithdrawalJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "reason_code", nullable = false)
	private String reasonCode;

	@Column(name = "reason_detail", length = 500)
	private String reasonDetail;

	@Column(name = "withdrawn_at", nullable = false)
	private Instant withdrawnAt;

	protected UserWithdrawalJpaEntity() {
	}

	private UserWithdrawalJpaEntity(Long userId, String reasonCode, String reasonDetail, Instant withdrawnAt) {
		this.userId = userId;
		this.reasonCode = reasonCode;
		this.reasonDetail = reasonDetail;
		this.withdrawnAt = withdrawnAt;
	}

	static UserWithdrawalJpaEntity create(
		Long userId,
		WithdrawalReasonCode reasonCode,
		String reasonDetail,
		Instant withdrawnAt
	) {
		return new UserWithdrawalJpaEntity(userId, reasonCode.name(), reasonDetail, withdrawnAt);
	}
}
