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
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.view.MyCrewInvitesView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CrewInviteController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class CrewInviteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetMyCrewInvitesUseCase getMyCrewInvitesUseCase;

	@MockitoBean
	private AcceptCrewInviteUseCase acceptCrewInviteUseCase;

	@MockitoBean
	private RejectCrewInviteUseCase rejectCrewInviteUseCase;

	@Test
	void returnsMyCrewInvites() throws Exception {
		when(getMyCrewInvitesUseCase.handle(GetMyCrewInvitesUseCase.Query.of(77L, 1, 10)))
			.thenReturn(MyCrewInvitesView.of(List.of(
				MyCrewInvitesView.Item.of(10L, 3L, "Crew Alpha", "leader-pot", CrewInviteStatus.PENDING),
				MyCrewInvitesView.Item.of(11L, 4L, "Crew Beta", "crew-master", CrewInviteStatus.REJECTED)
			), MyCrewInvitesView.Page.of(1, 10, true)));

		mockMvc.perform(
			get("/api/crew-invites/me?page=1&size=10")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].inviteId").value(10))
			.andExpect(jsonPath("$.items[0].crewId").value(3))
			.andExpect(jsonPath("$.items[0].crewName").value("Crew Alpha"))
			.andExpect(jsonPath("$.items[0].inviterNickname").value("leader-pot"))
			.andExpect(jsonPath("$.items[0].status").value("PENDING"))
			.andExpect(jsonPath("$.items[1].status").value("REJECTED"))
			.andExpect(jsonPath("$.pageInfo.page").value(1))
			.andExpect(jsonPath("$.pageInfo.size").value(10))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(true));
	}

	@Test
	void acceptsPendingCrewInvite() throws Exception {
		when(acceptCrewInviteUseCase.handle(AcceptCrewInviteUseCase.Command.of(10L, 77L)))
			.thenReturn(AcceptCrewInviteUseCase.Result.of(10L, 3L, "APPROVED"));

		mockMvc.perform(
			post("/api/crew-invites/10/accept")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.inviteId").value(10))
			.andExpect(jsonPath("$.crewId").value(3))
			.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void rejectsPendingCrewInvite() throws Exception {
		when(rejectCrewInviteUseCase.handle(RejectCrewInviteUseCase.Command.of(10L, 77L)))
			.thenReturn(RejectCrewInviteUseCase.Result.of(10L, 3L, "REJECTED"));

		mockMvc.perform(
			post("/api/crew-invites/10/reject")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.inviteId").value(10))
			.andExpect(jsonPath("$.crewId").value(3))
			.andExpect(jsonPath("$.status").value("REJECTED"));
	}

	@Test
	void returnsUnauthorizedWhenMyInvitesAreQueriedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crew-invites/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenCrewInviteIsAcceptedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/crew-invites/10/accept"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenCrewInviteIsRejectedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/crew-invites/10/reject"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
