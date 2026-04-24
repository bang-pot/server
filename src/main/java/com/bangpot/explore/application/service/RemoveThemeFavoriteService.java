package com.bangpot.explore.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.port.ThemeRepository;
import com.bangpot.explore.application.usecase.RemoveThemeFavoriteUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveThemeFavoriteService implements RemoveThemeFavoriteUseCase {
	private static final String ACCESS_DENIED_MESSAGE = "테마 찜을 해제할 권한이 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final ThemeRepository themeRepository;
	private final ThemeFavoriteRepository themeFavoriteRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), ACCESS_DENIED_MESSAGE);

		boolean deleted = themeFavoriteRepository.delete(command.userId(), command.themeId());
		if (deleted && !themeRepository.decreaseFavoriteCount(command.themeId())) {
			throw new ExploreThemeNotFoundException(command.themeId());
		}

		int favoriteCount = themeRepository.findActiveFavoriteCountById(command.themeId())
			.orElseThrow(() -> new ExploreThemeNotFoundException(command.themeId()));

		return Result.of(command.themeId(), false, favoriteCount);
	}
}
