package com.bangpot.explore.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.domain.ThemeFavorite;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaThemeFavoriteRepository implements ThemeFavoriteRepository {

	private final ThemeFavoriteJpaRepository themeFavoriteJpaRepository;

	@Override
	public boolean create(Long userId, Long themeId, Instant createdAt) {
		if (themeFavoriteJpaRepository.existsByUserIdAndThemeId(userId, themeId)) {
			return false;
		}
		themeFavoriteJpaRepository.save(ThemeFavorite.create(userId, themeId, createdAt));
		return true;
	}

	@Override
	public boolean delete(Long userId, Long themeId) {
		return themeFavoriteJpaRepository.deleteByUserIdAndThemeId(userId, themeId) > 0;
	}

	@Override
	public boolean exists(Long userId, Long themeId) {
		return themeFavoriteJpaRepository.existsByUserIdAndThemeId(userId, themeId);
	}

	@Override
	public Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
		if (themeIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(themeFavoriteJpaRepository.findThemeIdsByUserIdAndThemeIdIn(userId, themeIds));
	}
}
