package com.bangpot.explore.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.ThemeRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaThemeRepository implements ThemeRepository {

	private final ThemeJpaRepository themeJpaRepository;

	@Override
	public boolean increaseFavoriteCount(Long themeId) {
		return themeJpaRepository.increaseFavoriteCount(themeId) > 0;
	}

	@Override
	public boolean decreaseFavoriteCount(Long themeId) {
		return themeJpaRepository.decreaseFavoriteCount(themeId) > 0;
	}

	@Override
	public Optional<Integer> findActiveFavoriteCountById(Long themeId) {
		return themeJpaRepository.findActiveFavoriteCountById(themeId);
	}
}
