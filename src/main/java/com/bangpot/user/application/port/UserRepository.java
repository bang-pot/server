package com.bangpot.user.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.user.domain.User;

public interface UserRepository {

	Optional<User> findById(Long userId);

	boolean existsByNickname(String nickname);

	List<User> findCompletedUsersByNicknameContaining(String nickname);

	User save(User user);

	void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt);
}
