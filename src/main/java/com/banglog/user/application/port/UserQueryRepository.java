package com.banglog.user.application.port;

import com.banglog.user.domain.view.UserProfileView;
import com.banglog.user.domain.view.UserSearchView;

public interface UserQueryRepository {

	UserProfileView findMyProfileUserViewByUserId(Long userId);

	boolean existsCompletedUser(Long userId);

	UserSearchView searchUsersByNickname(String nickname, int page, int size);
}
