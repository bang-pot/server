package com.banglog.user.application.port;

import java.util.List;
import java.util.Optional;

import com.banglog.user.domain.User;

public interface UserRepository {

	Optional<User> findById(Long userId);

	boolean existsByNickname(String nickname);

	List<User> findCompletedUsersByNicknameContaining(String nickname);

	List<User> findAllCompletedUsers();

	User save(User user);

	boolean updateNickname(Long userId, String nickname);

	default boolean updateProfile(Long userId, String nickname, String profileImageUrl) {
		throw new UnsupportedOperationException("updateProfile is not supported");
	}

	void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt);
}
