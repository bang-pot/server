package com.banglog.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.banglog.user.application.port.UserRepository;
import com.banglog.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findById(Long userId) {
		return userJpaRepository.findByIdAndWithdrawnAtIsNull(userId).map(this::toDomain);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return userJpaRepository.existsByNicknameAndWithdrawnAtIsNull(nickname);
	}

	@Override
	public List<User> findCompletedUsersByNicknameContaining(String nickname) {
		String normalizedKeyword = normalizeKeyword(nickname);
		if (normalizedKeyword == null) {
			return List.of();
		}
		return userJpaRepository
			.findAllByNicknameContainingIgnoreCaseAndWithdrawnAtIsNullOrderByIdAsc(normalizedKeyword)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public List<User> findAllCompletedUsers() {
		return userJpaRepository.findAllByWithdrawnAtIsNullOrderByIdAsc()
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public User save(User user) {
		UserJpaEntity userJpaEntity = userJpaRepository.findById(user.getId())
			.orElseGet(() -> UserJpaEntity.create(user.getId(), user.getNickname()));
		userJpaEntity.updateProfile(user.getNickname(), user.getProfileImageUrl());
		return toDomain(userJpaRepository.save(userJpaEntity));
	}

	@Override
	public boolean updateNickname(Long userId, String nickname) {
		return userJpaRepository.updateNicknameById(userId, nickname) == 1;
	}

	@Override
	public boolean updateProfile(Long userId, String nickname, String profileImageUrl) {
		return userJpaRepository.updateProfileById(userId, nickname, profileImageUrl) == 1;
	}

	@Override
	public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		userJpaRepository.findById(userId)
			.ifPresent(user -> {
				user.withdraw(anonymizedNickname, withdrawnAt);
				userJpaRepository.save(user);
			});
	}

	private User toDomain(UserJpaEntity userJpaEntity) {
		return User.rehydrate(userJpaEntity.getId(), userJpaEntity.getNickname(), userJpaEntity.getProfileImageUrl());
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
