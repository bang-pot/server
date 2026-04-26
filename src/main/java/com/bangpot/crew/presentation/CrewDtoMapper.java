package com.bangpot.crew.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.DeleteCrewUseCase;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.crew.application.usecase.GetMeetingCreateCrewsUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.exception.CrewScheduleRequestValidationException;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;
import com.bangpot.crew.domain.view.CrewHubView;
import com.bangpot.crew.domain.view.CrewMembersView;

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

	static CrewDto.CrewHubResponse toResponse(CrewHubView result) {
		return new CrewDto.CrewHubResponse(
			result.crewId(),
			result.name(),
			result.description(),
			result.visibility().name(),
			result.imageUrl(),
			result.myRole(),
			result.hasNotice(),
			result.pendingJoinRequestCount()
		);
	}

	static GetCrewMembersUseCase.Query toMembersQuery(Long crewId, Long userId) {
		return GetCrewMembersUseCase.Query.of(crewId, userId);
	}

	static List<CrewDto.CrewMemberResponse> toMemberResponses(CrewMembersView view) {
		return view.items().stream()
			.map(item -> new CrewDto.CrewMemberResponse(
				item.userId(),
				item.nickname(),
				item.profileImageUrl(),
				item.bio(),
				item.gender(),
				item.escapeCount(),
				item.role(),
				item.joinedAt().toString()
			))
			.toList();
	}

	static CrewDto.MeetingCreateCrewsResponse toResponse(GetMeetingCreateCrewsUseCase.Result result) {
		return new CrewDto.MeetingCreateCrewsResponse(
			result.crews().stream()
				.map(crew -> new CrewDto.MeetingCreateCrewResponse(crew.crewId(), crew.crewName()))
				.toList()
		);
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

	static GetCrewScheduleUseCase.Query toScheduleQuery(Long crewId, Long userId, String from, String to) {
		LocalDate fromDate = parseDate("from", from);
		LocalDate toDate = parseDate("to", to);
		if (fromDate.isAfter(toDate)) {
			throw new CrewScheduleRequestValidationException(
				List.of(new ApiErrorField("to", "종료일은 시작일과 같거나 이후 날짜여야 합니다."))
			);
		}
		return GetCrewScheduleUseCase.Query.of(crewId, userId, fromDate.toString(), toDate.toString());
	}

	static CrewDto.CrewScheduleResponse toResponse(GetCrewScheduleUseCase.Result result) {
		return new CrewDto.CrewScheduleResponse(
			result.items().stream()
				.map(item -> new CrewDto.CrewScheduleItemResponse(
					item.meetingId(),
					item.themeName(),
					item.date(),
					item.time(),
					item.meetingStatus(),
					item.recruitmentStatus(),
					item.place(),
					item.participantCount(),
					item.isCanceled()
				))
				.toList()
		);
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

	static RemoveCrewMemberUseCase.Command toRemoveMemberCommand(Long crewId, Long leaderUserId, Long targetUserId) {
		return RemoveCrewMemberUseCase.Command.of(crewId, leaderUserId, targetUserId);
	}

	static CrewDto.RemoveCrewMemberResponse toResponse(RemoveCrewMemberUseCase.Result result) {
		return new CrewDto.RemoveCrewMemberResponse(result.crewId(), result.removedUserId());
	}

	static DeleteCrewUseCase.Command toDeleteCommand(
		Long crewId,
		Long leaderUserId,
		CrewDto.DeleteCrewRequest request
	) {
		return DeleteCrewUseCase.Command.of(crewId, leaderUserId, request.crewName());
	}

	static CrewDto.DeleteCrewResponse toResponse(DeleteCrewUseCase.Result result) {
		return new CrewDto.DeleteCrewResponse(result.crewId());
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

	static CancelCrewJoinRequestUseCase.Command toCancelCommand(Long userId, Long requestId) {
		return CancelCrewJoinRequestUseCase.Command.of(userId, requestId);
	}

	static CrewDto.CancelCrewJoinRequestResponse toResponse(CancelCrewJoinRequestUseCase.Result result) {
		return new CrewDto.CancelCrewJoinRequestResponse(result.requestId(), result.crewId());
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

	private static LocalDate parseDate(String field, String value) {
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException exception) {
			throw new CrewScheduleRequestValidationException(
				List.of(new ApiErrorField(field, field + " must be a valid date in yyyy-MM-dd format."))
			);
		}
	}
}
