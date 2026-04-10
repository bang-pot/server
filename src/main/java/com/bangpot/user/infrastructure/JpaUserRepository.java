package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

	private final UserAuthUserJpaRepository userAuthUserJpaRepository;

	@Override
	public Optional<User> findById(Long userId) {
		return userAuthUserJpaRepository.findById(userId).map(this::toDomain);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return userAuthUserJpaRepository.existsByNickname(nickname);
	}

	@Override
	public List<User> findCompletedUsersByNicknameContaining(String nickname) {
		String normalizedKeyword = normalizeKeyword(nickname);
		if (normalizedKeyword == null) {
			return userAuthUserJpaRepository.findAllByStatusAndNicknameIsNotNullOrderByIdAsc(AuthUserStatus.FULL)
				.stream()
				.map(this::toDomain)
				.toList();
		}
		return userAuthUserJpaRepository
			.findAllByStatusAndNicknameIsNotNullAndNicknameContainingIgnoreCaseOrderByIdAsc(
				AuthUserStatus.FULL,
				normalizedKeyword
			)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public User save(User user) {
		AuthUser authUser = userAuthUserJpaRepository.findById(user.getId())
			.orElseThrow(() -> new UserNotFoundException(user.getId()));
		authUser.updateNickname(user.getNickname());
		return toDomain(userAuthUserJpaRepository.save(authUser));
	}

	private User toDomain(AuthUser authUser) {
		return User.rehydrate(
			authUser.getId(),
			authUser.getNickname(),
			!authUser.requiresCompletion()
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
