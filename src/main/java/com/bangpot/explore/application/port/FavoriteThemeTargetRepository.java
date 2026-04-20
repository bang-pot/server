package com.bangpot.explore.application.port;

import java.util.Optional;

import com.bangpot.explore.domain.Theme;

public interface FavoriteThemeTargetRepository {

	Optional<Theme> findActiveById(Long themeId);

	Theme save(Theme theme);
}
