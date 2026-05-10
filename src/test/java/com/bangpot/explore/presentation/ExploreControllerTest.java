package com.bangpot.explore.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.usecase.AddThemeFavoriteUseCase;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.explore.application.usecase.RemoveThemeFavoriteUseCase;
import com.bangpot.explore.domain.view.ExploreFiltersView;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {ExploreController.class, ThemeFavoriteController.class})
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class ExploreControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetExploreThemesUseCase getExploreThemesUseCase;

	@MockitoBean
	private GetExploreFiltersUseCase getExploreFiltersUseCase;

	@MockitoBean
	private GetExploreThemeDetailUseCase getExploreThemeDetailUseCase;

	@MockitoBean
	private AddThemeFavoriteUseCase addThemeFavoriteUseCase;

	@MockitoBean
	private RemoveThemeFavoriteUseCase removeThemeFavoriteUseCase;

	@Test
	void returnsExploreThemeCardsWithoutAuthentication() throws Exception {
		when(getExploreThemesUseCase.handle(GetExploreThemesUseCase.Query.of(
			null,
			"deep",
			List.of("HORROR"),
			"Seoul",
			"Mapo",
			0,
			20
		))).thenReturn(ExploreThemeSearchView.of(
			List.of(
				ExploreThemeSearchView.Item.of(
					1L,
					"Deep Blue",
					101L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					List.of("HORROR", "THRILLER"),
					"https://image.example/deep-blue.jpg",
					4,
					"ACTIVE",
					"2-4 players",
					60,
					0,
					false
				)
			),
			ExploreThemeSearchView.PageInfo.of(0, 20, true, 1234L, 62)
		));

		mockMvc.perform(
			get("/api/explore/themes")
				.param("q", "deep")
				.param("genres", "HORROR")
				.param("region", "Seoul")
				.param("district", "Mapo")
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].themeId").value(1))
			.andExpect(jsonPath("$.items[0].themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.items[0].storeName").value("Seoul Escape Hongdae"))
			.andExpect(jsonPath("$.items[0].genres[0]").value("HORROR"))
			.andExpect(jsonPath("$.items[0].genres[1]").value("THRILLER"))
			.andExpect(jsonPath("$.items[0].favoriteCount").value(0))
			.andExpect(jsonPath("$.items[0].isFavorite").value(false))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
			.andExpect(jsonPath("$.pageInfo.totalElements").value(1234))
			.andExpect(jsonPath("$.pageInfo.totalPages").value(62));
	}

	@Test
	void returnsFilterOptionsWithoutAuthentication() throws Exception {
		when(getExploreFiltersUseCase.handle()).thenReturn(ExploreFiltersView.of(
			List.of("COMEDY", "HORROR"),
			List.of(
				ExploreFiltersView.Region.of("Busan", List.of("Haeundae")),
				ExploreFiltersView.Region.of("Seoul", List.of("Gangnam", "Mapo"))
			)
		));

		mockMvc.perform(get("/api/explore/filters"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.genres[0]").value("COMEDY"))
			.andExpect(jsonPath("$.regions[0].name").value("Busan"))
			.andExpect(jsonPath("$.regions[1].districts[1]").value("Mapo"));
	}

	@Test
	void returnsValidationErrorWhenPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/explore/themes")
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsThemeDetailWithoutAuthentication() throws Exception {
		when(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(null, 5L)))
			.thenReturn(ExploreThemeDetailView.of(
				5L,
				"Deep Blue",
				1L,
				"Seoul Escape Hongdae",
				"Seoul Mapo",
				List.of("HORROR"),
				"https://image.example/deep-blue.jpg",
				4,
				60,
				"Deep sea mystery theme",
				"https://example.com/deep-blue",
				false,
				List.of(
					ExploreThemeDetailView.RelatedTheme.of(
						1L,
						"Laugh Track",
						1L,
						"Seoul Escape Hongdae",
						"Seoul Mapo",
						List.of("COMEDY"),
						null,
						2,
						50,
						4,
						false
					)
				)
			));

		mockMvc.perform(get("/api/explore/themes/5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.themeId").value(5))
			.andExpect(jsonPath("$.themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.storeName").value("Seoul Escape Hongdae"))
			.andExpect(jsonPath("$.description").value("Deep sea mystery theme"))
			.andExpect(jsonPath("$.externalLink").value("https://example.com/deep-blue"))
			.andExpect(jsonPath("$.isFavorite").value(false))
			.andExpect(jsonPath("$.relatedThemes[0].themeId").value(1))
			.andExpect(jsonPath("$.relatedThemes[0].themeName").value("Laugh Track"))
			.andExpect(jsonPath("$.relatedThemes[0].favoriteCount").value(4))
			.andExpect(jsonPath("$.relatedThemes[0].isFavorite").value(false));
	}

	@Test
	void returnsFavoritedStateWhenAuthenticated() throws Exception {
		when(getExploreThemesUseCase.handle(GetExploreThemesUseCase.Query.of(
			7L,
			null,
			null,
			null,
			null,
			0,
			20
		))).thenReturn(ExploreThemeSearchView.of(
			List.of(
				ExploreThemeSearchView.Item.of(
					1L,
					"Deep Blue",
					101L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					List.of("HORROR"),
					"https://image.example/deep-blue.jpg",
					4,
					"ACTIVE",
					"2-4 players",
					60,
					3,
					true
				)
			),
			ExploreThemeSearchView.PageInfo.of(0, 20, false, 1L, 1)
		));
		when(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(7L, 5L)))
			.thenReturn(ExploreThemeDetailView.of(
				5L,
				"Deep Blue",
				1L,
				"Seoul Escape Hongdae",
				"Seoul Mapo",
				List.of("HORROR"),
				"https://image.example/deep-blue.jpg",
				4,
				60,
				"Deep sea mystery theme",
				"https://example.com/deep-blue",
				true,
				List.of()
			));

		mockMvc.perform(
			get("/api/explore/themes")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].isFavorite").value(true));

		mockMvc.perform(
			get("/api/explore/themes/5")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isFavorite").value(true));
	}

	@Test
	void returnsThemeNotFoundWhenThemeDoesNotExist() throws Exception {
		when(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(null, 999L)))
			.thenThrow(new ExploreThemeNotFoundException(999L));

		mockMvc.perform(get("/api/explore/themes/999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("EXPLORE_THEME_NOT_FOUND"));
	}

	@Test
	void favoritesThemeWhenAuthenticated() throws Exception {
		when(addThemeFavoriteUseCase.handle(AddThemeFavoriteUseCase.Command.of(7L, 5L)))
			.thenReturn(AddThemeFavoriteUseCase.Result.of(5L, true, 3));

		mockMvc.perform(
			post("/api/themes/5/favorite")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.themeId").value(5))
			.andExpect(jsonPath("$.isFavorite").value(true))
			.andExpect(jsonPath("$.favoriteCount").value(3));
	}

	@Test
	void unfavoritesThemeWhenAuthenticated() throws Exception {
		when(removeThemeFavoriteUseCase.handle(RemoveThemeFavoriteUseCase.Command.of(7L, 5L)))
			.thenReturn(RemoveThemeFavoriteUseCase.Result.of(5L, false, 2));

		mockMvc.perform(
			delete("/api/themes/5/favorite")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.themeId").value(5))
			.andExpect(jsonPath("$.isFavorite").value(false))
			.andExpect(jsonPath("$.favoriteCount").value(2));
	}

	@Test
	void returnsUnauthorizedWhenFavoriteIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/themes/5/favorite"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenFavoriteDeleteIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(delete("/api/themes/5/favorite"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
