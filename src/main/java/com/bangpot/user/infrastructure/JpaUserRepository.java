package com.bangpot.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.UserSearchResult;

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
			return userJpaRepository.findAllByWithdrawnAtIsNullOrderByIdAsc()
				.stream()
				.map(this::toDomain)
				.toList();
		}
		return userJpaRepository
			.findAllByNicknameContainingIgnoreCaseAndWithdrawnAtIsNullOrderByIdAsc(normalizedKeyword)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public List<UserSearchResult> searchByNickname(String nickname, int page, int size) {
		return userJpaRepository.searchByNickname(nickname, PageRequest.of(page, size)).stream()
			.map(this::toSearchResult)
			.toList();
	}

	@Override
	public long countByNickname(String nickname) {
		return userJpaRepository.countByNickname(nickname);
	}

	@Override
	public User save(User user) {
		UserJpaEntity userJpaEntity = userJpaRepository.findById(user.getId())
			.orElseGet(() -> UserJpaEntity.create(user.getId(), user.getNickname()));
		userJpaEntity.updateNickname(user.getNickname());
		return toDomain(userJpaRepository.save(userJpaEntity));
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
		return User.create(userJpaEntity.getId(), userJpaEntity.getNickname());
	}

	private UserSearchResult toSearchResult(UserJpaEntity userJpaEntity) {
		return UserSearchResult.of(
			userJpaEntity.getId(),
			userJpaEntity.getNickname(),
			userJpaEntity.getProfileImageUrl(),
			userJpaEntity.getBio(),
			userJpaEntity.getGender(),
			0
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
