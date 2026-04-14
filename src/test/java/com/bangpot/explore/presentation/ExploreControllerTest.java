package com.bangpot.explore.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ExploreController.class)
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

	@Test
	void returnsExploreThemeCardsWithoutAuthentication() throws Exception {
		when(getExploreThemesUseCase.handle(GetExploreThemesUseCase.Query.of(
			"deep",
			List.of("HORROR"),
			"Seoul",
			"Mapo",
			0,
			20
		))).thenReturn(GetExploreThemesUseCase.Result.of(
			List.of(
				GetExploreThemesUseCase.Item.of(
					1L,
					"Deep Blue",
					101L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					"HORROR",
					"https://image.example/deep-blue.jpg",
					4,
					"ACTIVE",
					"2-4 players",
					60,
					0,
					false
				)
			),
			GetExploreThemesUseCase.PageInfo.of(0, 20, true)
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
			.andExpect(jsonPath("$.items[0].genre").value("HORROR"))
			.andExpect(jsonPath("$.items[0].favoriteCount").value(0))
			.andExpect(jsonPath("$.items[0].isFavorited").value(false))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(true));
	}

	@Test
	void returnsFilterOptionsWithoutAuthentication() throws Exception {
		when(getExploreFiltersUseCase.handle()).thenReturn(GetExploreFiltersUseCase.Result.of(
			List.of("COMEDY", "HORROR"),
			List.of(
				GetExploreFiltersUseCase.RegionOption.of("Busan", List.of("Haeundae")),
				GetExploreFiltersUseCase.RegionOption.of("Seoul", List.of("Gangnam", "Mapo"))
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
		when(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(5L)))
			.thenReturn(GetExploreThemeDetailUseCase.Result.of(
				5L,
				"Deep Blue",
				1L,
				"Seoul Escape Hongdae",
				"Seoul Mapo",
				"HORROR",
				"https://image.example/deep-blue.jpg",
				4,
				60,
				"Deep sea mystery theme",
				"https://example.com/deep-blue",
				List.of(
					GetExploreThemeDetailUseCase.RelatedTheme.of(
						1L,
						"Laugh Track",
						1L,
						"Seoul Escape Hongdae",
						"Seoul Mapo",
						"COMEDY",
						null,
						2,
						50
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
			.andExpect(jsonPath("$.relatedThemes[0].themeId").value(1))
			.andExpect(jsonPath("$.relatedThemes[0].themeName").value("Laugh Track"));
	}

	@Test
	void returnsThemeNotFoundWhenThemeDoesNotExist() throws Exception {
		when(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(999L)))
			.thenThrow(new ExploreThemeNotFoundException(999L));

		mockMvc.perform(get("/api/explore/themes/999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("EXPLORE_THEME_NOT_FOUND"));
	}
}
