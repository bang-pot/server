package com.bangpot.user.application.usecase;

import com.bangpot.user.domain.view.MyWithdrawalCheckView;

public interface GetMyWithdrawalCheckUseCase {

	MyWithdrawalCheckView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
