package com.bangpot.user.infrastructure;

import org.springframework.stereotype.Repository;

import com.bangpot.user.application.port.UserWithdrawalRepository;
import com.bangpot.user.domain.UserWithdrawal;
import com.bangpot.user.domain.WithdrawalReasonCode;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserWithdrawalRepository implements UserWithdrawalRepository {

	private final UserWithdrawalJpaRepository userWithdrawalJpaRepository;

	@Override
	public UserWithdrawal save(UserWithdrawal userWithdrawal) {
		UserWithdrawalJpaEntity saved = userWithdrawalJpaRepository.save(
			UserWithdrawalJpaEntity.create(
				userWithdrawal.getUserId(),
				userWithdrawal.getReasonCode(),
				userWithdrawal.getReasonDetail(),
				userWithdrawal.getWithdrawnAt()
			)
		);
		return UserWithdrawal.create(
			saved.getUserId(),
			WithdrawalReasonCode.valueOf(saved.getReasonCode()),
			saved.getReasonDetail(),
			saved.getWithdrawnAt()
		);
	}
}
