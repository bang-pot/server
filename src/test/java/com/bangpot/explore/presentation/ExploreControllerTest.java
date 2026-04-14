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
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
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

	@Test
	void returnsExploreThemeCardsWithoutAuthentication() throws Exception {
		when(getExploreThemesUseCase.handle(GetExploreThemesUseCase.Query.of(
			"홍대",
			List.of("HORROR"),
			"서울",
			"마포구",
			0,
			20
		))).thenReturn(GetExploreThemesUseCase.Result.of(
			List.of(
				GetExploreThemesUseCase.Item.of(
					1L,
					"Deep Blue",
					101L,
					"Seoul Escape Hongdae",
					"서울 마포구",
					"HORROR",
					"https://image.example/deep-blue.jpg",
					4,
					"ACTIVE",
					"2-4인",
					60,
					0,
					false
				)
			),
			GetExploreThemesUseCase.PageInfo.of(0, 20, true)
		));

		mockMvc.perform(
			get("/api/explore/themes")
				.param("q", "홍대")
				.param("genres", "HORROR")
				.param("region", "서울")
				.param("district", "마포구")
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
				GetExploreFiltersUseCase.RegionOption.of("부산", List.of("해운대구")),
				GetExploreFiltersUseCase.RegionOption.of("서울", List.of("강남구", "마포구"))
			)
		));

		mockMvc.perform(get("/api/explore/filters"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.genres[0]").value("COMEDY"))
			.andExpect(jsonPath("$.regions[0].name").value("부산"))
			.andExpect(jsonPath("$.regions[1].districts[1]").value("마포구"));
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
}
