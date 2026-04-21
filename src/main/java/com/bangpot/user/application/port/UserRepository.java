package com.bangpot.user.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.user.domain.User;

public interface UserRepository {

	Optional<User> findById(Long userId);

	boolean existsByNickname(String nickname);

	List<User> findCompletedUsersByNicknameContaining(String nickname);

	default List<User> findAllCompletedUsers() {
		throw new UnsupportedOperationException("findAllCompletedUsers is not implemented");
	}

	User save(User user);

	default void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		throw new UnsupportedOperationException("withdrawById is not implemented");
	}
}
