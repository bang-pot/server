package com.bangpot.user.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.user.domain.User;
import com.bangpot.user.domain.UserSearchResult;

public interface UserRepository {

	Optional<User> findById(Long userId);

	boolean existsByNickname(String nickname);

	List<User> findCompletedUsersByNicknameContaining(String nickname);

	List<UserSearchResult> searchByNickname(String nickname, int page, int size);

	long countByNickname(String nickname);

	User save(User user);

	void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt);
}
