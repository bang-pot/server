package com.bangpot.crew.presentation;

import java.util.List;

import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;

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

	static List<CrewDto.PublicCrewCardResponse> toPublicCardResponses(List<GetPublicCrewCardsUseCase.View> views) {
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

	static GetCrewHubUseCase.Query toHubQuery(Long crewId, Long userId) {
		return GetCrewHubUseCase.Query.of(crewId, userId);
	}

	static CrewDto.CrewHubResponse toResponse(GetCrewHubUseCase.Result result) {
		return new CrewDto.CrewHubResponse(
			result.crewId(),
			result.name(),
			result.description(),
			result.visibility(),
			result.imageUrl(),
			result.myRole(),
			result.hasNotice(),
			result.pendingJoinRequestCount()
		);
	}

	static GetCrewMembersUseCase.Query toMembersQuery(Long crewId, Long userId) {
		return GetCrewMembersUseCase.Query.of(crewId, userId);
	}

	static List<CrewDto.CrewMemberResponse> toMemberResponses(List<GetCrewMembersUseCase.View> views) {
		return views.stream()
			.map(view -> new CrewDto.CrewMemberResponse(
				view.userId(),
				view.nickname(),
				view.profileImageUrl(),
				view.bio(),
				view.gender(),
				view.escapeCount(),
				view.role(),
				view.joinedAt()
			))
			.toList();
	}

	static GetCrewPoliciesUseCase.Query toPoliciesQuery(Long crewId, Long userId) {
		return GetCrewPoliciesUseCase.Query.of(crewId, userId);
	}

	static List<CrewDto.CrewPolicyResponse> toPolicyResponses(List<GetCrewPoliciesUseCase.View> views) {
		return views.stream()
			.map(view -> new CrewDto.CrewPolicyResponse(
				view.policyId(),
				view.title(),
				view.content()
			))
			.toList();
	}

	static UpdateCrewVisibilityUseCase.Command toVisibilityCommand(
		Long crewId,
		Long leaderUserId,
		CrewDto.UpdateCrewVisibilityRequest request
	) {
		return UpdateCrewVisibilityUseCase.Command.of(crewId, leaderUserId, request.visibility());
	}

	static CrewDto.UpdateCrewVisibilityResponse toResponse(UpdateCrewVisibilityUseCase.Result result) {
		return new CrewDto.UpdateCrewVisibilityResponse(result.crewId(), result.visibility());
	}

	static LeaveCrewUseCase.Command toLeaveCommand(Long crewId, Long userId) {
		return LeaveCrewUseCase.Command.of(crewId, userId);
	}

	static CrewDto.LeaveCrewResponse toResponse(LeaveCrewUseCase.Result result) {
		return new CrewDto.LeaveCrewResponse(result.crewId());
	}

	static TransferCrewLeadershipUseCase.Command toTransferCommand(
		Long crewId,
		Long leaderUserId,
		CrewDto.TransferCrewLeadershipRequest request
	) {
		return TransferCrewLeadershipUseCase.Command.of(crewId, leaderUserId, request.targetUserId());
	}

	static CrewDto.TransferCrewLeadershipResponse toResponse(TransferCrewLeadershipUseCase.Result result) {
		return new CrewDto.TransferCrewLeadershipResponse(result.crewId(), result.leaderUserId());
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

	static GetPendingCrewJoinRequestsUseCase.Query toQuery(Long crewId, Long leaderUserId) {
		return GetPendingCrewJoinRequestsUseCase.Query.of(crewId, leaderUserId);
	}

	static GetCrewJoinRequestsUseCase.Query toManagementQuery(Long crewId, Long leaderUserId) {
		return GetCrewJoinRequestsUseCase.Query.of(crewId, leaderUserId);
	}

	static List<CrewDto.PendingCrewJoinRequestResponse> toPendingResponses(
		List<GetPendingCrewJoinRequestsUseCase.View> views
	) {
		return views.stream()
			.map(view -> new CrewDto.PendingCrewJoinRequestResponse(
				view.requestId(),
				view.userId(),
				view.nickname()
			))
			.toList();
	}

	static List<CrewDto.CrewJoinRequestResponse> toJoinRequestResponses(List<GetCrewJoinRequestsUseCase.View> views) {
		return views.stream()
			.map(view -> new CrewDto.CrewJoinRequestResponse(
				view.requestId(),
				view.userId(),
				view.nickname(),
				view.message(),
				view.status()
			))
			.toList();
	}

	static ApproveCrewJoinRequestUseCase.Command toApproveCommand(Long crewId, Long requestId, Long leaderUserId) {
		return ApproveCrewJoinRequestUseCase.Command.of(crewId, requestId, leaderUserId);
	}

	static CrewDto.ApproveCrewJoinRequestResponse toResponse(ApproveCrewJoinRequestUseCase.Result result) {
		return new CrewDto.ApproveCrewJoinRequestResponse(
			result.crewId(),
			result.requestId(),
			result.userId(),
			result.role()
		);
	}

	static RejectCrewJoinRequestUseCase.Command toRejectCommand(Long crewId, Long requestId, Long leaderUserId) {
		return RejectCrewJoinRequestUseCase.Command.of(crewId, requestId, leaderUserId);
	}

	static CrewDto.RejectCrewJoinRequestResponse toResponse(RejectCrewJoinRequestUseCase.Result result) {
		return new CrewDto.RejectCrewJoinRequestResponse(result.crewId(), result.requestId());
	}

	static GetCrewInviteCandidatesUseCase.Query toInviteCandidatesQuery(
		Long crewId,
		Long leaderUserId,
		String nickname
	) {
		return GetCrewInviteCandidatesUseCase.Query.of(crewId, leaderUserId, nickname);
	}

	static List<CrewDto.CrewInviteCandidateResponse> toInviteCandidateResponses(
		List<GetCrewInviteCandidatesUseCase.View> views
	) {
		return views.stream()
			.map(view -> new CrewDto.CrewInviteCandidateResponse(view.userId(), view.nickname()))
			.toList();
	}

	static CreateCrewInviteUseCase.Command toCreateInviteCommand(
		Long crewId,
		Long inviterUserId,
		CrewDto.CreateCrewInviteRequest request
	) {
		return CreateCrewInviteUseCase.Command.of(crewId, inviterUserId, request.targetUserId());
	}

	static CrewDto.CreateCrewInviteResponse toResponse(CreateCrewInviteUseCase.Result result) {
		return new CrewDto.CreateCrewInviteResponse(result.crewId(), result.targetUserId(), result.status());
	}
}
