package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.domain.view.UserSearchView;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchUsersService implements SearchUsersUseCase {

	private static final int MIN_PAGE = 0;
	private static final int MIN_SIZE = 1;
	private static final int MAX_SIZE = 50;

	private final UserQueryRepository userQueryRepository;

	@Override
	public UserSearchView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		int normalizedPage = normalizePage(query.page());
		int normalizedSize = normalizeSize(query.size());
		String normalizedKeyword = normalizeKeyword(query.keyword());
		if (normalizedKeyword == null) {
			return UserSearchView.of(
				java.util.List.of(),
				UserSearchView.Page.of(normalizedPage, normalizedSize, 0L, 0)
			);
		}

		return userQueryRepository.searchUsersByNickname(normalizedKeyword, normalizedPage, normalizedSize);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : escapeLikeKeyword(trimmed);
	}

	private int normalizePage(int page) {
		return Math.max(page, MIN_PAGE);
	}

	private int normalizeSize(int size) {
		if (size < MIN_SIZE) {
			return MIN_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}

	private String escapeLikeKeyword(String keyword) {
		return keyword
			.replace("\\", "\\\\")
			.replace("%", "\\%")
			.replace("_", "\\_");
	}
}
