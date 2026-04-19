package com.bangpot.user.application.port;

import com.bangpot.user.domain.UserWithdrawal;

public interface UserWithdrawalRepository {

	UserWithdrawal save(UserWithdrawal userWithdrawal);
}
