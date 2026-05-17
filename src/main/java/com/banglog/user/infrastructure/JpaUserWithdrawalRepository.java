package com.banglog.user.infrastructure;

import org.springframework.stereotype.Repository;

import com.banglog.user.application.port.UserWithdrawalRepository;
import com.banglog.user.domain.UserWithdrawal;
import com.banglog.user.domain.WithdrawalReasonCode;

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
