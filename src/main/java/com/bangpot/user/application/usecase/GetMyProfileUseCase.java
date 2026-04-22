package com.bangpot.user.application.usecase;

import com.bangpot.user.domain.view.MyProfileView;

public interface GetMyProfileUseCase {

	MyProfileView handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
