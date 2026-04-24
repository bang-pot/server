package com.bangpot.explore.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.ThemeFavoriteRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaThemeFavoriteRepository implements ThemeFavoriteRepository {

	private final ThemeFavoriteJpaRepository themeFavoriteJpaRepository;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public boolean create(Long userId, Long themeId, Instant createdAt) {
		return jdbcTemplate.update("""
			insert into theme_favorites (user_id, theme_id, created_at)
			values (?, ?, ?)
			on conflict (user_id, theme_id) do nothing
			""", userId, themeId, Timestamp.from(createdAt)) > 0;
	}

	@Override
	public boolean delete(Long userId, Long themeId) {
		return themeFavoriteJpaRepository.deleteByUserIdAndThemeId(userId, themeId) > 0;
	}

	@Override
	public Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
		if (themeIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(themeFavoriteJpaRepository.findThemeIdsByUserIdAndThemeIdIn(userId, themeIds));
	}
}
