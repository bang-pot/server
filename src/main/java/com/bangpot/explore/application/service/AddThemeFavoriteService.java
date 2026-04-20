package com.bangpot.explore.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.port.FavoriteThemeTargetRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.usecase.AddThemeFavoriteUseCase;
import com.bangpot.explore.domain.Theme;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AddThemeFavoriteService implements AddThemeFavoriteUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "테마를 찜할 권한이 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final FavoriteThemeTargetRepository favoriteThemeTargetRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), ACCESS_DENIED_MESSAGE);

		Theme theme = favoriteThemeTargetRepository.findActiveById(command.themeId())
			.orElseThrow(() -> new ExploreThemeNotFoundException(command.themeId()));

		boolean created = themeFavoriteRepository.create(command.userId(), command.themeId(), Instant.now());
		if (created) {
			theme.increaseFavoriteCount();
			favoriteThemeTargetRepository.save(theme);
		}

		return Result.of(theme.getId(), true, theme.getFavoriteCount());
	}
}
