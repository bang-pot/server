package com.banglog.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.explore.application.port.ThemeFavoriteQueryRepository;
import com.banglog.explore.domain.view.MyFavoriteThemesView;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.GetMyFavoriteThemesUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyFavoriteThemesService implements GetMyFavoriteThemesUseCase {

	private final UserQueryRepository userQueryRepository;
	private final ThemeFavoriteQueryRepository themeFavoriteQueryRepository;

	@Override
	public MyFavoriteThemesView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}
		return themeFavoriteQueryRepository.findMyFavoriteThemesViewByUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
