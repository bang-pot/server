package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.port.FavoriteThemeSummaryReadRepository;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.bangpot.user.domain.User;

class GetMyFavoriteThemesSummaryServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsFavoriteThemeSummaryForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot", true));
		favoriteThemeSummaryReadRepository.putView(
			7L,
			FavoriteThemeSummaryReadRepository.View.of(
				java.util.List.of(
					FavoriteThemeSummaryReadRepository.Item.of(
						101L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-101.jpg",
						13,
						true
					)
				),
				6L
			)
		);

		GetMyFavoriteThemesSummaryUseCase.Result result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).themeId()).isEqualTo(101L);
		assertThat(result.items().get(0).themeName()).isEqualTo("Deep Blue");
		assertThat(result.items().get(0).storeName()).isEqualTo("Seoul Escape");
		assertThat(result.items().get(0).regionName()).isEqualTo("서울");
		assertThat(result.items().get(0).thumbnailUrl()).isEqualTo("https://cdn.example.com/theme-101.jpg");
		assertThat(result.items().get(0).favoriteCount()).isEqualTo(13);
		assertThat(result.items().get(0).isFavorite()).isTrue();
		assertThat(result.totalCount()).isEqualTo(6L);
		assertThat(result.hasMore()).isTrue();
	}

	@Test
	void returnsEmptyFavoriteThemeSummaryWhenNoFavoritesExist() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot", true));

		GetMyFavoriteThemesSummaryUseCase.Result result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.totalCount()).isZero();
		assertThat(result.hasMore()).isFalse();
	}

	@Test
	void rejectsFavoriteSummaryLookupForTempUser() {
		authUserRepository.save(tempUser(7L));

		assertThatThrownBy(() -> getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsFavoriteSummaryLookupWhenUserRowIsMissing() {
		authUserRepository.save(fullUser(7L, "bangpot"));

		assertThatThrownBy(() -> getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsFavoriteSummaryLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(77L)
		)).isInstanceOf(AuthUserNotFoundException.class);
	}
}
