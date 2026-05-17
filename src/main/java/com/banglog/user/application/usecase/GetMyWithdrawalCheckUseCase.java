package com.banglog.user.application.usecase;

import com.banglog.user.domain.view.MyWithdrawalCheckView;

public interface GetMyWithdrawalCheckUseCase {

	MyWithdrawalCheckView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
