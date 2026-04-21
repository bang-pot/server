package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.FavoriteThemeSummaryReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyFavoriteThemesSummaryService implements GetMyFavoriteThemesSummaryUseCase {

	private static final int SUMMARY_LIMIT = 5;

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final FavoriteThemeSummaryReadRepository favoriteThemeSummaryReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.isTemp()) {
			throw new AccessDeniedException("가입 완료 사용자만 이용할 수 있습니다.");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		FavoriteThemeSummaryReadRepository.View view = favoriteThemeSummaryReadRepository.load(user.getId(), SUMMARY_LIMIT);
		return Result.of(
			view.items().stream()
				.map(item -> Item.of(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionName(),
					item.thumbnailUrl(),
					item.favoriteCount(),
					item.isFavorite()
				))
				.toList(),
			view.totalCount(),
			view.totalCount() > SUMMARY_LIMIT
		);
	}
}
