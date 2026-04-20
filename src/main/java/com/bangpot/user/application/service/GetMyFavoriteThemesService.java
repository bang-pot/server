package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.MyFavoriteThemeReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyFavoriteThemesService implements GetMyFavoriteThemesUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final MyFavoriteThemeReadRepository myFavoriteThemeReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		MyFavoriteThemeReadRepository.SearchResult searchResult = myFavoriteThemeReadRepository.search(
			user.getId(),
			query.page(),
			query.size()
		);

		return Result.of(
			searchResult.items().stream()
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
			PageInfo.of(
				searchResult.pageInfo().page(),
				searchResult.pageInfo().size(),
				searchResult.pageInfo().hasNext()
			)
		);
	}
}
