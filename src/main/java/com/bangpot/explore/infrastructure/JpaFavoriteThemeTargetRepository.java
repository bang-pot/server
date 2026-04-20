package com.bangpot.explore.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.FavoriteThemeTargetRepository;
import com.bangpot.explore.domain.Theme;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaFavoriteThemeTargetRepository implements FavoriteThemeTargetRepository {

	private final ThemeJpaRepository themeJpaRepository;

	@Override
	public Optional<Theme> findActiveById(Long themeId) {
		return themeJpaRepository.findByIdAndActiveTrue(themeId);
	}

	@Override
	public Theme save(Theme theme) {
		return themeJpaRepository.save(theme);
	}
}
