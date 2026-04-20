package com.bangpot.home.presentation;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.home.application.usecase.GetHomeUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = HomeController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetHomeUseCase getHomeUseCase;

	@Test
	void returnsGuestHomeWithoutAuthentication() throws Exception {
		when(getHomeUseCase.handle(GetHomeUseCase.Query.of(null)))
			.thenReturn(GetHomeUseCase.Result.of(
				false,
				GetHomeUseCase.Cta.of(false, true),
				GetHomeUseCase.MyCrewsSection.of(List.of(), 0L),
				GetHomeUseCase.UpcomingMeetingsSection.of(List.of(), 0L),
				GetHomeUseCase.PublicCrewPreviewSection.of(
					List.of(GetHomeUseCase.PublicCrewPreviewItem.of(31L, "Alpha Crew", null, 12L, true))
				),
				GetHomeUseCase.ThemeExplorePreviewSection.of(
					List.of(GetHomeUseCase.ThemeExplorePreviewItem.of(
						101L,
						"Deep Blue",
						"Room Escape",
						"Seoul Mapo",
						"https://cdn.example.com/theme.jpg",
						7,
						false
					))
				)
			));

		mockMvc.perform(get("/api/home"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isLoggedIn").value(false))
			.andExpect(jsonPath("$.cta.canCreateCrew").value(false))
			.andExpect(jsonPath("$.cta.canExplorePublicCrews").value(true))
			.andExpect(jsonPath("$.myCrews.items").isArray())
			.andExpect(jsonPath("$.myCrews.items").isEmpty())
			.andExpect(jsonPath("$.myCrews.totalCount").value(0))
			.andExpect(jsonPath("$.upcomingMeetings.items").isArray())
			.andExpect(jsonPath("$.upcomingMeetings.items").isEmpty())
			.andExpect(jsonPath("$.upcomingMeetings.totalCount").value(0))
			.andExpect(jsonPath("$.publicCrewPreview.items[0].crewId").value(31))
			.andExpect(jsonPath("$.themeExplorePreview.items[0].themeId").value(101))
			.andExpect(jsonPath("$.themeExplorePreview.items[0].favoriteCount").value(7))
			.andExpect(jsonPath("$.themeExplorePreview.items[0].isFavorite").value(false));
	}

	@Test
	void returnsLoggedInHomeWithPersonalizedSections() throws Exception {
		when(getHomeUseCase.handle(GetHomeUseCase.Query.of(7L)))
			.thenReturn(GetHomeUseCase.Result.of(
				true,
				GetHomeUseCase.Cta.of(true, true),
				GetHomeUseCase.MyCrewsSection.of(
					List.of(GetHomeUseCase.MyCrewItem.of(11L, "Alpha Crew")),
					3L
				),
				GetHomeUseCase.UpcomingMeetingsSection.of(
					List.of(GetHomeUseCase.UpcomingMeetingItem.of(
						101L, "Friday Escape", 11L, "Alpha Crew", "2026-04-20", "19:00", "RECRUITING"
					)),
					4L
				),
				GetHomeUseCase.PublicCrewPreviewSection.of(List.of()),
				GetHomeUseCase.ThemeExplorePreviewSection.of(List.of())
			));

		mockMvc.perform(
			get("/api/home")
				.principal(new UsernamePasswordAuthenticationToken(7L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isLoggedIn").value(true))
			.andExpect(jsonPath("$.cta.canCreateCrew").value(true))
			.andExpect(jsonPath("$.myCrews.items[0].crewId").value(11))
			.andExpect(jsonPath("$.myCrews.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.myCrews.totalCount").value(3))
			.andExpect(jsonPath("$.upcomingMeetings.items[0].meetingId").value(101))
			.andExpect(jsonPath("$.upcomingMeetings.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.upcomingMeetings.totalCount").value(4));
	}
}
