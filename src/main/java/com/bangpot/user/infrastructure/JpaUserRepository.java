package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findById(Long userId) {
		return userJpaRepository.findById(userId).map(this::toDomain);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return userJpaRepository.existsByNickname(nickname);
	}

	@Override
	public List<User> findCompletedUsersByNicknameContaining(String nickname) {
		String normalizedKeyword = normalizeKeyword(nickname);
		if (normalizedKeyword == null) {
			return userJpaRepository.findAllByOrderByIdAsc()
				.stream()
				.map(this::toDomain)
				.toList();
		}
		return userJpaRepository
			.findAllByNicknameContainingIgnoreCaseOrderByIdAsc(normalizedKeyword)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public User save(User user) {
		UserJpaEntity userJpaEntity = userJpaRepository.findById(user.getId())
			.orElseGet(() -> UserJpaEntity.create(user.getId(), user.getNickname()));
		userJpaEntity.updateNickname(user.getNickname());
		return toDomain(userJpaRepository.save(userJpaEntity));
	}

	private User toDomain(UserJpaEntity userJpaEntity) {
		return User.rehydrate(userJpaEntity.getId(), userJpaEntity.getNickname(), true);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
