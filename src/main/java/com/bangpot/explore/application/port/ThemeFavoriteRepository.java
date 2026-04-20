package com.bangpot.explore.application.port;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ThemeFavoriteRepository {

	boolean create(Long userId, Long themeId, Instant createdAt);

	boolean delete(Long userId, Long themeId);

	boolean exists(Long userId, Long themeId);

	Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds);
}
