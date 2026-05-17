package com.banglog.user.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.SearchUsersUseCase;
import com.banglog.user.domain.view.UserSearchView;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchUsersService implements SearchUsersUseCase {

	private static final int MIN_PAGE = 0;
	private static final int MIN_SIZE = 1;
	private static final int MAX_SIZE = 50;

	private final MeetingQueryRepository meetingQueryRepository;
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
				List.of(),
				UserSearchView.Page.of(normalizedPage, normalizedSize, 0L, 0)
			);
		}

		UserSearchView queryResult = userQueryRepository.searchUsersByNickname(
			normalizedKeyword,
			normalizedPage,
			normalizedSize
		);
		Map<Long, Integer> completedMeetingCountsByUserId = meetingQueryRepository.countCompletedByUserIds(
			queryResult.items().stream()
				.map(UserSearchView.Item::userId)
				.toList()
		);
		return UserSearchView.of(
			queryResult.items().stream()
				.map(item -> UserSearchView.Item.of(
					item.userId(),
					item.nickname(),
					item.profileImageUrl(),
					item.bio(),
					item.gender(),
					completedMeetingCountsByUserId.getOrDefault(item.userId(), 0)
				))
				.toList(),
			queryResult.page()
		);
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
