package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.explore.domain.view.MyFavoriteThemesView;
import com.banglog.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.banglog.user.domain.User;

class GetMyFavoriteThemesServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyFavoriteThemesForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		themeFavoriteRepository.putView(
			7L,
			MyFavoriteThemesView.of(
				List.of(
					MyFavoriteThemesView.Item.of(
						901L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-901.jpg",
						"공포",
						3,
						90,
						"깊은 바닷속 비밀을 추적하는 테마입니다.",
						12,
						true
					)
				),
				MyFavoriteThemesView.Page.of(0, 20, false)
			)
		);

		MyFavoriteThemesView result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).themeId()).isEqualTo(901L);
		assertThat(result.items().get(0).themeName()).isEqualTo("Deep Blue");
		assertThat(result.items().get(0).storeName()).isEqualTo("Seoul Escape");
		assertThat(result.items().get(0).regionName()).isEqualTo("서울");
		assertThat(result.items().get(0).thumbnailUrl()).isEqualTo("https://cdn.example.com/theme-901.jpg");
		assertThat(result.items().get(0).genreName()).isEqualTo("공포");
		assertThat(result.items().get(0).difficulty()).isEqualTo(3);
		assertThat(result.items().get(0).runningTimeMinutes()).isEqualTo(90);
		assertThat(result.items().get(0).description()).isEqualTo("깊은 바닷속 비밀을 추적하는 테마입니다.");
		assertThat(result.items().get(0).favoriteCount()).isEqualTo(12);
		assertThat(result.items().get(0).isFavorite()).isTrue();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void defaultsFavoriteThemeListToEmptyWhenNoFavoritesExist() {
		userRepository.save(User.create(7L, "banglog"));

		MyFavoriteThemesView result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void rejectsFavoriteThemeLookupForTempUser() {
		assertThatThrownBy(() -> getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsFavoriteThemeLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
