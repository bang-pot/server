package com.bangpot.crew.presentation;

import java.util.List;

import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;

final class CrewInviteDtoMapper {

	private CrewInviteDtoMapper() {
	}

	static GetMyCrewInvitesUseCase.Query toQuery(Long userId) {
		return GetMyCrewInvitesUseCase.Query.of(userId);
	}

	static List<CrewInviteDto.MyCrewInviteResponse> toResponses(List<GetMyCrewInvitesUseCase.View> views) {
		return views.stream()
			.map(view -> new CrewInviteDto.MyCrewInviteResponse(
				view.inviteId(),
				view.crewId(),
				view.crewName(),
				view.inviterNickname(),
				view.status()
			))
			.toList();
	}

	static AcceptCrewInviteUseCase.Command toAcceptCommand(Long inviteId, Long userId) {
		return AcceptCrewInviteUseCase.Command.of(inviteId, userId);
	}

	static CrewInviteDto.ProcessCrewInviteResponse toResponse(AcceptCrewInviteUseCase.Result result) {
		return new CrewInviteDto.ProcessCrewInviteResponse(result.inviteId(), result.crewId(), result.status());
	}

	static RejectCrewInviteUseCase.Command toRejectCommand(Long inviteId, Long userId) {
		return RejectCrewInviteUseCase.Command.of(inviteId, userId);
	}

	static CrewInviteDto.ProcessCrewInviteResponse toResponse(RejectCrewInviteUseCase.Result result) {
		return new CrewInviteDto.ProcessCrewInviteResponse(result.inviteId(), result.crewId(), result.status());
	}
}
