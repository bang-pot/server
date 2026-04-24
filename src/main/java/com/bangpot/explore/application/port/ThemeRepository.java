package com.bangpot.explore.application.port;

import java.util.Optional;

public interface ThemeRepository {

	boolean increaseFavoriteCount(Long themeId);

	boolean decreaseFavoriteCount(Long themeId);

	Optional<Integer> findActiveFavoriteCountById(Long themeId);
}
