package com.bangpot.user.application.port;

import com.bangpot.user.domain.view.UserProfileView;
import com.bangpot.user.domain.view.UserSearchView;

public interface UserQueryRepository {

	UserProfileView findMyProfileUserViewByUserId(Long userId);

	boolean existsCompletedUser(Long userId);

	UserSearchView searchUsersByNickname(String nickname, int page, int size);
}
