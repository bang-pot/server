package com.bangpot.user.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.port.UserSearchReadRepository;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchUsersService implements SearchUsersUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final UserSearchReadRepository userSearchReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.isTemp()) {
			throw new AccessDeniedException("가입 완료 사용자만 회원 검색을 할 수 있습니다.");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		String normalizedKeyword = normalizeKeyword(query.keyword());
		if (normalizedKeyword == null) {
			return Result.of(List.of());
		}

		return Result.of(
			userSearchReadRepository.search(normalizedKeyword, query.size()).stream()
				.map(item -> Item.of(
					item.userId(),
					item.nickname(),
					item.profileImageUrl(),
					item.bio(),
					item.gender(),
					item.escapeCount()
				))
				.toList()
		);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
