package com.banglog.explore.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.explore.application.exception.ExploreThemeNotFoundException;
import com.banglog.explore.application.port.ThemeFavoriteRepository;
import com.banglog.explore.application.port.ThemeRepository;
import com.banglog.explore.application.usecase.AddThemeFavoriteUseCase;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AddThemeFavoriteService implements AddThemeFavoriteUseCase {
	private static final String ACCESS_DENIED_MESSAGE = "테마를 찜할 권한이 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final ThemeRepository themeRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), ACCESS_DENIED_MESSAGE);

		boolean created = themeFavoriteRepository.create(command.userId(), command.themeId(), Instant.now());
		if (created && !themeRepository.increaseFavoriteCount(command.themeId())) {
			throw new ExploreThemeNotFoundException(command.themeId());
		}

		int favoriteCount = themeRepository.findActiveFavoriteCountById(command.themeId())
			.orElseThrow(() -> new ExploreThemeNotFoundException(command.themeId()));

		return Result.of(command.themeId(), true, favoriteCount);
	}
}
