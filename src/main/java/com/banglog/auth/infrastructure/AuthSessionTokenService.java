package com.banglog.auth.infrastructure;

import java.util.Optional;

public interface AuthSessionTokenService {

	String createAccessToken(Long userId);

	Optional<Long> resolveUserId(String accessToken);
}
