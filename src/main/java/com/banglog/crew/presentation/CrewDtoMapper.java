package com.banglog.crew.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.banglog.common.error.ApiErrorField;
import com.banglog.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.banglog.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.banglog.crew.application.usecase.CreateCrewInviteUseCase;
import com.banglog.crew.application.usecase.CreateCrewUseCase;
import com.banglog.crew.application.usecase.DeleteCrewUseCase;
import com.banglog.crew.application.usecase.GetCrewHubUseCase;
import com.banglog.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.banglog.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.banglog.crew.application.usecase.GetCrewMembersUseCase;
import com.banglog.crew.application.usecase.GetCrewPoliciesUseCase;
import com.banglog.crew.application.usecase.GetCrewScheduleUseCase;
import com.banglog.crew.application.usecase.GetExploreCrewCardsUseCase;
import com.banglog.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.banglog.crew.application.exception.CrewScheduleRequestValidationException;
import com.banglog.crew.application.usecase.LeaveCrewUseCase;
import com.banglog.crew.application.usecase.RemoveCrewMemberUseCase;
import com.banglog.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.banglog.crew.application.usecase.RequestCrewJoinUseCase;
import com.banglog.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.banglog.crew.application.usecase.UpdateCrewVisibilityUseCase;
import com.banglog.crew.domain.ExploreCrewSort;
import com.banglog.crew.domain.view.CrewHubView;
import com.banglog.crew.domain.view.CrewInviteCandidatesView;
import com.banglog.crew.domain.view.CrewJoinView;
import com.banglog.crew.domain.view.CrewJoinRequestsView;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.crew.domain.view.CrewPoliciesView;
import com.banglog.crew.domain.view.ExploreCrewCardsView;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.crew.domain.view.PendingCrewJoinRequestsView;
import com.banglog.meeting.domain.view.CrewScheduleView;

final class CrewDtoMapper {

	private CrewDtoMapper() {
	}

	static CreateCrewUseCase.Command toCommand(Long userId, CrewDto.CreateCrewRequest request) {
		return CreateCrewUseCase.Command.of(
			userId,
			request.name(),
			request.description(),
			request.visibility(),
			request.imageUploadId()
		);
	}

	static CrewDto.CreateCrewResponse toResponse(CreateCrewUseCase.Result result) {
		return new CrewDto.CreateCrewResponse(result.crewId(), result.name(), result.myRole());
	}

	static GetExploreCrewCardsUseCase.Query toExploreQuery(int page, int size, String keyword, String sort) {
		return GetExploreCrewCardsUseCase.Query.of(page, size, keyword, ExploreCrewSort.from(sort));
	}

	static CrewDto.ExploreCrewCardsResponse toExploreCardResponse(ExploreCrewCardsView view) {
		return new CrewDto.ExploreCrewCardsResponse(
			view.items().stream()
				.map(item -> new CrewDto.ExploreCrewCardResponse(
					item.crewId(),
					item.name(),
					item.description(),
					item.imageUrl(),
					item.visibility().name(),
					item.leaderNickname(),
					item.memberCount()
				))
				.toList(),
			new CrewDto.PageInfoResponse(
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
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

	static CrewDto.MeetingCreateCrewsResponse toResponse(MeetingCreateCrewsView result) {
		return new CrewDto.MeetingCreateCrewsResponse(
			result.items().stream()
				.map(item -> new CrewDto.MeetingCreateCrewResponse(item.crewId(), item.crewName()))
				.toList()
		);
	}

	static GetCrewPoliciesUseCase.Query toPoliciesQuery(Long crewId, Long userId) {
		return GetCrewPoliciesUseCase.Query.of(crewId, userId);
	}

	static List<CrewDto.CrewPolicyResponse> toPolicyResponses(CrewPoliciesView view) {
		return view.items().stream()
			.map(item -> new CrewDto.CrewPolicyResponse(
				item.policyId(),
				item.title(),
				item.content()
			))
			.toList();
	}

	static GetCrewScheduleUseCase.Query toScheduleQuery(Long crewId, Long userId, String from, String to) {
		LocalDate fromDate = parseDate("from", from);
		LocalDate toDate = parseDate("to", to);
		return GetCrewScheduleUseCase.Query.of(crewId, userId, fromDate, toDate);
	}

	static CrewDto.CrewScheduleResponse toResponse(CrewScheduleView result) {
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

	static CrewDto.CrewJoinViewResponse toResponse(CrewJoinView result) {
		return new CrewDto.CrewJoinViewResponse(
			result.crewId(),
			result.name(),
			result.description(),
			result.visibility().name(),
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

	static GetPendingCrewJoinRequestsUseCase.Query toQuery(Long crewId, Long leaderUserId, int page, int size) {
		return GetPendingCrewJoinRequestsUseCase.Query.of(crewId, leaderUserId, page, size);
	}

	static GetCrewJoinRequestsUseCase.Query toManagementQuery(Long crewId, Long leaderUserId, int page, int size) {
		return GetCrewJoinRequestsUseCase.Query.of(crewId, leaderUserId, page, size);
	}

	static CrewDto.PendingCrewJoinRequestsResponse toPendingResponses(
		PendingCrewJoinRequestsView view
	) {
		return new CrewDto.PendingCrewJoinRequestsResponse(
			view.items().stream()
				.map(item -> new CrewDto.PendingCrewJoinRequestResponse(
					item.requestId(),
					item.userId(),
					item.nickname()
				))
				.toList(),
			new CrewDto.PageInfoResponse(
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
	}

	static CrewDto.CrewJoinRequestsResponse toJoinRequestResponses(CrewJoinRequestsView view) {
		return new CrewDto.CrewJoinRequestsResponse(
			view.items().stream()
				.map(item -> new CrewDto.CrewJoinRequestResponse(
					item.requestId(),
					item.userId(),
					item.nickname(),
					item.message(),
					item.status()
				))
				.toList(),
			new CrewDto.PageInfoResponse(
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
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
		String nickname,
		int page,
		int size
	) {
		return GetCrewInviteCandidatesUseCase.Query.of(crewId, leaderUserId, nickname, page, size);
	}

	static CrewDto.CrewInviteCandidatesResponse toInviteCandidateResponses(
		CrewInviteCandidatesView view
	) {
		return new CrewDto.CrewInviteCandidatesResponse(
			view.items().stream()
				.map(item -> new CrewDto.CrewInviteCandidateResponse(item.userId(), item.nickname()))
				.toList(),
			new CrewDto.PageInfoResponse(
				view.page().page(),
				view.page().size(),
				view.page().hasNext()
			)
		);
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
				List.of(new ApiErrorField(field, field + "은(는) yyyy-MM-dd 형식의 올바른 날짜여야 합니다."))
			);
		}
	}
}
