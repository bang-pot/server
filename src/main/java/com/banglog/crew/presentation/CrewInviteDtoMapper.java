package com.banglog.crew.presentation;

import com.banglog.crew.application.usecase.AcceptCrewInviteUseCase;
import com.banglog.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.banglog.crew.application.usecase.RejectCrewInviteUseCase;
import com.banglog.crew.domain.view.MyCrewInvitesView;

final class CrewInviteDtoMapper {

	private CrewInviteDtoMapper() {
	}

	static GetMyCrewInvitesUseCase.Query toQuery(Long userId, int page, int size) {
		return GetMyCrewInvitesUseCase.Query.of(userId, page, size);
	}

	static CrewInviteDto.MyCrewInvitesResponse toResponse(MyCrewInvitesView view) {
		return new CrewInviteDto.MyCrewInvitesResponse(
			view.items().stream()
				.map(item -> new CrewInviteDto.MyCrewInviteResponse(
					item.inviteId(),
					item.crewId(),
					item.crewName(),
					item.inviterNickname(),
					item.status().name()
				))
				.toList(),
			new CrewInviteDto.PageInfoResponse(
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
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
