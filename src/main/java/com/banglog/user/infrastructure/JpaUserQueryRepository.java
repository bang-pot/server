package com.banglog.user.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.domain.view.UserProfileView;
import com.banglog.user.domain.view.UserSearchView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserQueryRepository implements UserQueryRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public UserProfileView findMyProfileUserViewByUserId(Long userId) {
		return userJpaRepository.findByIdAndWithdrawnAtIsNull(userId)
			.map(user -> UserProfileView.of(
				user.getId(),
				user.getNickname(),
				user.getProfileImageUrl()
			))
			.orElse(null);
	}

	@Override
	public boolean existsCompletedUser(Long userId) {
		return userJpaRepository.existsByIdAndWithdrawnAtIsNull(userId);
	}

	@Override
	public UserSearchView searchUsersByNickname(String nickname, int page, int size) {
		Page<UserSearchView.Item> resultPage = userJpaRepository.searchRowsByNickname(
			nickname,
			PageRequest.of(page, size)
		);
		return UserSearchView.of(
			resultPage.getContent(),
			UserSearchView.Page.of(page, size, resultPage.getTotalElements(), resultPage.getTotalPages())
		);
	}
}
