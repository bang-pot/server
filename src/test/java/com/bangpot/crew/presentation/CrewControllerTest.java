package com.bangpot.crew.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewRole;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CrewController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class CrewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateCrewUseCase createCrewUseCase;

	@MockitoBean
	private GetCrewJoinViewUseCase getCrewJoinViewUseCase;

	@MockitoBean
	private GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;

	@MockitoBean
	private RequestCrewJoinUseCase requestCrewJoinUseCase;

	@Test
	void createsCrewForAuthenticatedFullUser() throws Exception {
		when(createCrewUseCase.handle(CreateCrewUseCase.Command.of(
			77L,
			"Crew Alpha",
			"public crew",
			null,
			null
		))).thenReturn(CreateCrewUseCase.Result.of(1L, "Crew Alpha", CrewRole.LEADER));

		mockMvc.perform(
			post("/api/crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "name": "Crew Alpha",
					  "description": "public crew"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.name").value("Crew Alpha"))
			.andExpect(jsonPath("$.myRole").value("LEADER"));
	}

	@Test
	void returnsUnauthorizedWhenCrewCreationIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/crews")
				.contentType("application/json")
				.content("""
					{
					  "name": "Crew Alpha"
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenCrewNameIsBlank() throws Exception {
		mockMvc.perform(
			post("/api/crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "name": "   "
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
	}

	@Test
	void returnsValidationErrorWhenVisibilityIsInvalid() throws Exception {
		mockMvc.perform(
			post("/api/crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "name": "Crew Alpha",
					  "visibility": "SECRET"
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("visibility"));
	}

	@Test
	void returnsFieldErrorWhenCrewNameIsDuplicate() throws Exception {
		when(createCrewUseCase.handle(CreateCrewUseCase.Command.of(
			77L,
			"Crew Alpha",
			null,
			null,
			null
		))).thenThrow(new DuplicateCrewNameException("Crew Alpha"));

		mockMvc.perform(
			post("/api/crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "name": "Crew Alpha"
					}
					""")
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CREW_DUPLICATE_NAME"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
	}

	@Test
	void returnsCrewJoinViewForGuestUser() throws Exception {
		when(getCrewJoinViewUseCase.handle(GetCrewJoinViewUseCase.Query.of(1L, null)))
			.thenReturn(GetCrewJoinViewUseCase.Result.of(
				1L,
				"Crew Alpha",
				"public crew",
				"PUBLIC",
				null,
				CrewJoinViewStatus.GUEST
			));

		mockMvc.perform(get("/api/crews/1/join"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.name").value("Crew Alpha"))
			.andExpect(jsonPath("$.visibility").value("PUBLIC"))
			.andExpect(jsonPath("$.myStatus").value("GUEST"));
	}

	@Test
	void returnsPublicCrewCardsForCardList() throws Exception {
		when(getPublicCrewCardsUseCase.handle()).thenReturn(List.of(
			GetPublicCrewCardsUseCase.View.of(1L, "Crew Alpha", "public crew", "PUBLIC", null),
			GetPublicCrewCardsUseCase.View.of(2L, "Crew Beta", "night runners", "PUBLIC", "https://image.example/beta.png")
		));

		mockMvc.perform(get("/api/crews/public"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].crewId").value(1))
			.andExpect(jsonPath("$[0].name").value("Crew Alpha"))
			.andExpect(jsonPath("$[0].description").value("public crew"))
			.andExpect(jsonPath("$[0].visibility").value("PUBLIC"))
			.andExpect(jsonPath("$[0].imageUrl").doesNotExist())
			.andExpect(jsonPath("$[1].crewId").value(2))
			.andExpect(jsonPath("$[1].imageUrl").value("https://image.example/beta.png"));
	}

	@Test
	void createsJoinRequestForAuthenticatedUser() throws Exception {
		when(requestCrewJoinUseCase.handle(RequestCrewJoinUseCase.Command.of(1L, 77L, "let me join")))
			.thenReturn(RequestCrewJoinUseCase.Result.of(1L, CrewJoinViewStatus.PENDING));

		mockMvc.perform(
			post("/api/crews/1/join-requests")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "message": "let me join"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.myStatus").value("PENDING"));
	}

	@Test
	void returnsValidationErrorWhenJoinMessageIsTooLong() throws Exception {
		mockMvc.perform(
			post("/api/crews/1/join-requests")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "message": "%s"
					}
					""".formatted("a".repeat(201)))
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("message"));
	}

	@Test
	void returnsUnauthorizedWhenJoinRequestIsSubmittedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/crews/1/join-requests")
				.contentType("application/json")
				.content("""
					{
					  "message": "let me join"
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
