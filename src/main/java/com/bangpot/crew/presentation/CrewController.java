package com.bangpot.crew.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.crew.application.usecase.GetExploreCrewCardsUseCase;
import com.bangpot.crew.application.usecase.GetMeetingCreateCrewsUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.DeleteCrewUseCase;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;
import com.bangpot.crew.domain.view.CrewJoinView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
class CrewController {

	private final CreateCrewUseCase createCrewUseCase;
	private final GetCrewJoinViewUseCase getCrewJoinViewUseCase;
	private final GetExploreCrewCardsUseCase getExploreCrewCardsUseCase;
	private final GetCrewHubUseCase getCrewHubUseCase;
	private final GetCrewMembersUseCase getCrewMembersUseCase;
	private final GetMeetingCreateCrewsUseCase getMeetingCreateCrewsUseCase;
	private final GetCrewPoliciesUseCase getCrewPoliciesUseCase;
	private final GetCrewScheduleUseCase getCrewScheduleUseCase;
	private final UpdateCrewVisibilityUseCase updateCrewVisibilityUseCase;
	private final LeaveCrewUseCase leaveCrewUseCase;
	private final RemoveCrewMemberUseCase removeCrewMemberUseCase;
	private final DeleteCrewUseCase deleteCrewUseCase;
	private final TransferCrewLeadershipUseCase transferCrewLeadershipUseCase;
	private final RequestCrewJoinUseCase requestCrewJoinUseCase;
	private final GetPendingCrewJoinRequestsUseCase getPendingCrewJoinRequestsUseCase;
	private final GetCrewJoinRequestsUseCase getCrewJoinRequestsUseCase;
	private final CancelCrewJoinRequestUseCase cancelCrewJoinRequestUseCase;
	private final GetCrewInviteCandidatesUseCase getCrewInviteCandidatesUseCase;
	private final CreateCrewInviteUseCase createCrewInviteUseCase;
	private final ApproveCrewJoinRequestUseCase approveCrewJoinRequestUseCase;
	private final RejectCrewJoinRequestUseCase rejectCrewJoinRequestUseCase;

	@PostMapping
	ResponseEntity<CrewDto.CreateCrewResponse> create(
		Authentication authentication,
		@Valid @RequestBody CrewDto.CreateCrewRequest request
	) {
		CreateCrewUseCase.Result result = createCrewUseCase.handle(
			CrewDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@GetMapping("/explore")
	ResponseEntity<CrewDto.ExploreCrewCardsResponse> getExploreCrewCards(
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size,
		@RequestParam(required = false) String keyword,
		@RequestParam(value = "sort", defaultValue = "LATEST")
		@Pattern(
			regexp = "LATEST|OLDEST|MEMBER_COUNT_DESC|MEMBER_COUNT_ASC",
			message = "sort는 LATEST, OLDEST, MEMBER_COUNT_DESC, MEMBER_COUNT_ASC 중 하나여야 합니다."
		) String sort
	) {
		return ResponseEntity.ok(CrewDtoMapper.toExploreCardResponse(
			getExploreCrewCardsUseCase.handle(CrewDtoMapper.toExploreQuery(page, size, keyword, sort))
		));
	}

	@GetMapping("/{crewId}")
	ResponseEntity<CrewDto.CrewHubResponse> getCrewHub(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			getCrewHubUseCase.handle(CrewDtoMapper.toHubQuery(crewId, requireAuthenticatedUserId(authentication)))
		));
	}

	@GetMapping("/{crewId}/members")
	ResponseEntity<List<CrewDto.CrewMemberResponse>> getCrewMembers(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toMemberResponses(
			getCrewMembersUseCase.handle(
				CrewDtoMapper.toMembersQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/me/meeting-create")
	ResponseEntity<CrewDto.MeetingCreateCrewsResponse> getMeetingCreateCrews(Authentication authentication) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			getMeetingCreateCrewsUseCase.handle(
				GetMeetingCreateCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/{crewId}/policies")
	ResponseEntity<List<CrewDto.CrewPolicyResponse>> getCrewPolicies(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toPolicyResponses(
			getCrewPoliciesUseCase.handle(
				CrewDtoMapper.toPoliciesQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/{crewId}/schedule")
	ResponseEntity<CrewDto.CrewScheduleResponse> getCrewSchedule(
		@PathVariable Long crewId,
		@RequestParam("from") @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "from은 yyyy-MM-dd 형식이어야 합니다.") String from,
		@RequestParam("to") @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "to는 yyyy-MM-dd 형식이어야 합니다.") String to,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			getCrewScheduleUseCase.handle(
				CrewDtoMapper.toScheduleQuery(crewId, requireAuthenticatedUserId(authentication), from, to)
			)
		));
	}

	@PatchMapping("/{crewId}/visibility")
	ResponseEntity<CrewDto.UpdateCrewVisibilityResponse> updateCrewVisibility(
		@PathVariable Long crewId,
		Authentication authentication,
		@RequestBody CrewDto.UpdateCrewVisibilityRequest request
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			updateCrewVisibilityUseCase.handle(
				CrewDtoMapper.toVisibilityCommand(crewId, requireAuthenticatedUserId(authentication), request)
			)
		));
	}

	@PostMapping("/{crewId}/leave")
	ResponseEntity<CrewDto.LeaveCrewResponse> leaveCrew(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			leaveCrewUseCase.handle(
				CrewDtoMapper.toLeaveCommand(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{crewId}/members/{targetUserId}/remove")
	ResponseEntity<CrewDto.RemoveCrewMemberResponse> removeCrewMember(
		@PathVariable Long crewId,
		@PathVariable Long targetUserId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			removeCrewMemberUseCase.handle(
				CrewDtoMapper.toRemoveMemberCommand(crewId, requireAuthenticatedUserId(authentication), targetUserId)
			)
		));
	}

	@PostMapping("/{crewId}/delete")
	ResponseEntity<CrewDto.DeleteCrewResponse> deleteCrew(
		@PathVariable Long crewId,
		Authentication authentication,
		@Valid @RequestBody CrewDto.DeleteCrewRequest request
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			deleteCrewUseCase.handle(
				CrewDtoMapper.toDeleteCommand(crewId, requireAuthenticatedUserId(authentication), request)
			)
		));
	}

	@PostMapping("/{crewId}/transfer-leadership")
	ResponseEntity<CrewDto.TransferCrewLeadershipResponse> transferLeadership(
		@PathVariable Long crewId,
		Authentication authentication,
		@Valid @RequestBody CrewDto.TransferCrewLeadershipRequest request
	) {
		return ResponseEntity.ok(CrewDtoMapper.toResponse(
			transferCrewLeadershipUseCase.handle(
				CrewDtoMapper.toTransferCommand(crewId, requireAuthenticatedUserId(authentication), request)
			)
		));
	}

	@GetMapping("/{crewId}/join")
	ResponseEntity<CrewDto.CrewJoinViewResponse> getJoinView(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crewId, optionalAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@PostMapping("/{crewId}/join-requests")
	ResponseEntity<CrewDto.RequestCrewJoinResponse> requestJoin(
		@PathVariable Long crewId,
		Authentication authentication,
		@Valid @RequestBody CrewDto.RequestCrewJoinRequest request
	) {
		RequestCrewJoinUseCase.Result result = requestCrewJoinUseCase.handle(
			CrewDtoMapper.toCommand(crewId, requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@GetMapping("/{crewId}/join-requests/pending")
	ResponseEntity<CrewDto.PendingCrewJoinRequestsResponse> getPendingJoinRequests(
		@PathVariable Long crewId,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page??0 ?댁긽?댁뼱???⑸땲??") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size??1 ?댁긽?댁뼱???⑸땲??")
		@Max(value = 50, message = "size??50 ?댄븯?ъ빞 ?⑸땲??") int size,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toPendingResponses(
			getPendingCrewJoinRequestsUseCase.handle(
				CrewDtoMapper.toQuery(crewId, requireAuthenticatedUserId(authentication), page, size)
			)
		));
	}

	@GetMapping("/{crewId}/join-requests")
	ResponseEntity<CrewDto.CrewJoinRequestsResponse> getJoinRequests(
		@PathVariable Long crewId,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toJoinRequestResponses(
			getCrewJoinRequestsUseCase.handle(
				CrewDtoMapper.toManagementQuery(crewId, requireAuthenticatedUserId(authentication), page, size)
			)
		));
	}

	@GetMapping("/{crewId}/invite-candidates")
	ResponseEntity<CrewDto.CrewInviteCandidatesResponse> getInviteCandidates(
		@PathVariable Long crewId,
		@RequestParam(required = false) String nickname,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toInviteCandidateResponses(
			getCrewInviteCandidatesUseCase.handle(
				CrewDtoMapper.toInviteCandidatesQuery(
					crewId,
					requireAuthenticatedUserId(authentication),
					nickname,
					page,
					size
				)
			)
		));
	}

	@PostMapping("/{crewId}/invites")
	ResponseEntity<CrewDto.CreateCrewInviteResponse> createInvite(
		@PathVariable Long crewId,
		Authentication authentication,
		@Valid @RequestBody CrewDto.CreateCrewInviteRequest request
	) {
		CreateCrewInviteUseCase.Result result = createCrewInviteUseCase.handle(
			CrewDtoMapper.toCreateInviteCommand(crewId, requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@PostMapping("/{crewId}/join-requests/{requestId}/approve")
	ResponseEntity<CrewDto.ApproveCrewJoinRequestResponse> approveJoinRequest(
		@PathVariable Long crewId,
		@PathVariable Long requestId,
		Authentication authentication
	) {
		ApproveCrewJoinRequestUseCase.Result result = approveCrewJoinRequestUseCase.handle(
			CrewDtoMapper.toApproveCommand(crewId, requestId, requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@PostMapping("/{crewId}/join-requests/{requestId}/reject")
	ResponseEntity<CrewDto.RejectCrewJoinRequestResponse> rejectJoinRequest(
		@PathVariable Long crewId,
		@PathVariable Long requestId,
		Authentication authentication
	) {
		RejectCrewJoinRequestUseCase.Result result = rejectCrewJoinRequestUseCase.handle(
			CrewDtoMapper.toRejectCommand(crewId, requestId, requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	@PostMapping("/join-requests/{requestId}/cancel")
	ResponseEntity<CrewDto.CancelCrewJoinRequestResponse> cancelJoinRequest(
		@PathVariable Long requestId,
		Authentication authentication
	) {
		CancelCrewJoinRequestUseCase.Result result = cancelCrewJoinRequestUseCase.handle(
			CrewDtoMapper.toCancelCommand(requireAuthenticatedUserId(authentication), requestId)
		);
		return ResponseEntity.ok(CrewDtoMapper.toResponse(result));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		Long userId = optionalAuthenticatedUserId(authentication);
		if (userId == null) {
			throw new UnauthenticatedException();
		}
		return userId;
	}

	private Long optionalAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			return null;
		}
		return userId;
	}
}
