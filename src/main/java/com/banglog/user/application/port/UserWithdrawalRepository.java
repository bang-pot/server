package com.banglog.user.application.port;

import com.banglog.user.domain.UserWithdrawal;

public interface UserWithdrawalRepository {

	UserWithdrawal save(UserWithdrawal userWithdrawal);
}
