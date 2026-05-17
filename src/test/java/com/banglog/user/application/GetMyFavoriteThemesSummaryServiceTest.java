package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;
import com.banglog.explore.domain.view.MyFavoriteThemesView;
import com.banglog.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.banglog.user.domain.User;

class GetMyFavoriteThemesSummaryServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsFavoriteThemeSummaryForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		themeFavoriteRepository.putView(
			7L,
			MyFavoriteThemesView.of(
				List.of(
					MyFavoriteThemesView.Item.of(
						101L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-101.jpg",
						13,
						true
					),
					MyFavoriteThemesView.Item.of(
						102L,
						"Red Room",
						"Busan Escape",
						"부산",
						"https://cdn.example.com/theme-102.jpg",
						11,
						true
					),
					MyFavoriteThemesView.Item.of(
						103L,
						"Golden Key",
						"Incheon Escape",
						"인천",
						"https://cdn.example.com/theme-103.jpg",
						9,
						true
					),
					MyFavoriteThemesView.Item.of(
						104L,
						"Blue Night",
						"Daegu Escape",
						"대구",
						"https://cdn.example.com/theme-104.jpg",
						7,
						true
					),
					MyFavoriteThemesView.Item.of(
						105L,
						"Secret Hall",
						"Daejeon Escape",
						"대전",
						"https://cdn.example.com/theme-105.jpg",
						5,
						true
					),
					MyFavoriteThemesView.Item.of(
						106L,
						"Final Door",
						"Gwangju Escape",
						"광주",
						"https://cdn.example.com/theme-106.jpg",
						3,
						true
					)
				),
				MyFavoriteThemesView.Page.of(0, 20, false)
			)
		);

		MyFavoriteThemesSummaryView result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		);

		assertThat(result.items()).hasSize(5);
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
		userRepository.save(User.create(7L, "banglog"));

		MyFavoriteThemesSummaryView result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.totalCount()).isZero();
		assertThat(result.hasMore()).isFalse();
	}

	@Test
	void rejectsFavoriteSummaryLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(7L)
		)).isInstanceOf(AccessDeniedException.class);
	}
}
