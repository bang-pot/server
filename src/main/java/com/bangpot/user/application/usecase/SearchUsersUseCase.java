package com.bangpot.user.application.usecase;

import java.util.List;

import com.bangpot.user.domain.UserSearchResult;

public interface SearchUsersUseCase {

	Result handle(Query query);

	record Query(Long userId, String keyword, int page, int size) {
		public static Query of(Long userId, String keyword, int page, int size) {
			return new Query(userId, keyword, page, size);
		}
	}

	record Result(List<UserSearchResult> items, PageInfo pageInfo) {
		public static Result of(List<UserSearchResult> items, PageInfo pageInfo) {
			return new Result(items, pageInfo);
		}
	}

	record PageInfo(
		int page,
		int size,
		long totalElements,
		int totalPages
	) {
		public static PageInfo of(int page, int size, long totalElements, int totalPages) {
			return new PageInfo(page, size, totalElements, totalPages);
		}
	}
}
