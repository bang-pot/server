package com.bangpot.crew.presentation;

import java.util.List;

import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;

final class CrewDtoMapper {

	private CrewDtoMapper() {
	}

	static CreateCrewUseCase.Command toCommand(Long userId, CrewDto.CreateCrewRequest request) {
		return CreateCrewUseCase.Command.of(
			userId,
			request.name(),
			request.description(),
			request.visibility(),
			request.imageUrl()
		);
	}

	static CrewDto.CreateCrewResponse toResponse(CreateCrewUseCase.Result result) {
		return new CrewDto.CreateCrewResponse(result.crewId(), result.name(), result.myRole());
	}

	static List<CrewDto.PublicCrewCardResponse> toResponses(List<GetPublicCrewCardsUseCase.View> views) {
		return views.stream()
			.map(view -> new CrewDto.PublicCrewCardResponse(
				view.crewId(),
				view.name(),
				view.description(),
				view.visibility(),
				view.imageUrl()
			))
			.toList();
	}

	static CrewDto.CrewJoinViewResponse toResponse(GetCrewJoinViewUseCase.Result result) {
		return new CrewDto.CrewJoinViewResponse(
			result.crewId(),
			result.name(),
			result.description(),
			result.visibility(),
			result.imageUrl(),
			result.myStatus()
		);
	}

	static RequestCrewJoinUseCase.Command toCommand(
		Long crewId,
		Long userId,
		CrewDto.RequestCrewJoinRequest request
	) {
		return RequestCrewJoinUseCase.Command.of(crewId, userId, request.message());
	}

	static CrewDto.RequestCrewJoinResponse toResponse(RequestCrewJoinUseCase.Result result) {
		return new CrewDto.RequestCrewJoinResponse(result.crewId(), result.myStatus());
	}
}
