package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.MyFavoriteThemeReadRepository;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.bangpot.user.domain.User;

class GetMyFavoriteThemesServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyFavoriteThemesForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		myFavoriteThemeReadRepository.putResult(
			7L,
			MyFavoriteThemeReadRepository.SearchResult.of(
				List.of(
					MyFavoriteThemeReadRepository.Item.of(
						901L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-901.jpg",
						12,
						true
					)
				),
				MyFavoriteThemeReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyFavoriteThemesUseCase.Result result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).themeId()).isEqualTo(901L);
		assertThat(result.items().get(0).themeName()).isEqualTo("Deep Blue");
		assertThat(result.items().get(0).storeName()).isEqualTo("Seoul Escape");
		assertThat(result.items().get(0).regionName()).isEqualTo("서울");
		assertThat(result.items().get(0).thumbnailUrl()).isEqualTo("https://cdn.example.com/theme-901.jpg");
		assertThat(result.items().get(0).favoriteCount()).isEqualTo(12);
		assertThat(result.items().get(0).isFavorite()).isTrue();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsFavoriteThemeListToEmptyWhenNoFavoritesExist() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		GetMyFavoriteThemesUseCase.Result result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsFavoriteThemeLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsFavoriteThemeLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsFavoriteThemeLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
