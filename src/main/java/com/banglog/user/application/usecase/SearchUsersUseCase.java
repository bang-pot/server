package com.banglog.user.application.usecase;

import com.banglog.user.domain.view.UserSearchView;

public interface SearchUsersUseCase {

	UserSearchView handle(Query query);

	record Query(Long userId, String keyword, int page, int size) {
		public static Query of(Long userId, String keyword, int page, int size) {
			return new Query(userId, keyword, page, size);
		}
	}
}
