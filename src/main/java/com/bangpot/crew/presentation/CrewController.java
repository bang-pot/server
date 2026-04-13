package com.bangpot.crew.presentation;

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
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
class CrewController {

	private final CreateCrewUseCase createCrewUseCase;
	private final GetCrewJoinViewUseCase getCrewJoinViewUseCase;
	private final GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;
	private final GetCrewHubUseCase getCrewHubUseCase;
	private final GetCrewMembersUseCase getCrewMembersUseCase;
	private final GetCrewPoliciesUseCase getCrewPoliciesUseCase;
	private final UpdateCrewVisibilityUseCase updateCrewVisibilityUseCase;
	private final LeaveCrewUseCase leaveCrewUseCase;
	private final RemoveCrewMemberUseCase removeCrewMemberUseCase;
	private final TransferCrewLeadershipUseCase transferCrewLeadershipUseCase;
	private final RequestCrewJoinUseCase requestCrewJoinUseCase;
	private final GetPendingCrewJoinRequestsUseCase getPendingCrewJoinRequestsUseCase;
	private final GetCrewJoinRequestsUseCase getCrewJoinRequestsUseCase;
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

	@GetMapping("/public")
	ResponseEntity<List<CrewDto.PublicCrewCardResponse>> getPublicCrewCards() {
		return ResponseEntity.ok(CrewDtoMapper.toPublicCardResponses(getPublicCrewCardsUseCase.handle()));
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
		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
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
	ResponseEntity<List<CrewDto.PendingCrewJoinRequestResponse>> getPendingJoinRequests(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toPendingResponses(
			getPendingCrewJoinRequestsUseCase.handle(
				CrewDtoMapper.toQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/{crewId}/join-requests")
	ResponseEntity<List<CrewDto.CrewJoinRequestResponse>> getJoinRequests(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toJoinRequestResponses(
			getCrewJoinRequestsUseCase.handle(
				CrewDtoMapper.toManagementQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/{crewId}/invite-candidates")
	ResponseEntity<List<CrewDto.CrewInviteCandidateResponse>> getInviteCandidates(
		@PathVariable Long crewId,
		@RequestParam(required = false) String nickname,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toInviteCandidateResponses(
			getCrewInviteCandidatesUseCase.handle(
				CrewDtoMapper.toInviteCandidatesQuery(crewId, requireAuthenticatedUserId(authentication), nickname)
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
