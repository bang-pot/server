package com.bangpot.user.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.domain.view.UserSearchView;
import com.bangpot.user.domain.view.UserProfileView;

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
		Page<UserJpaEntity> resultPage = userJpaRepository.searchByNickname(nickname, PageRequest.of(page, size));
		return UserSearchView.of(
			resultPage.getContent().stream()
				.map(this::toSearchViewItem)
				.toList(),
			UserSearchView.Page.of(page, size, resultPage.getTotalElements(), resultPage.getTotalPages())
		);
	}

	private UserSearchView.Item toSearchViewItem(UserJpaEntity userJpaEntity) {
		return UserSearchView.Item.of(
			userJpaEntity.getId(),
			userJpaEntity.getNickname(),
			userJpaEntity.getProfileImageUrl(),
			userJpaEntity.getBio(),
			userJpaEntity.getGender(),
			0
		);
	}
}
