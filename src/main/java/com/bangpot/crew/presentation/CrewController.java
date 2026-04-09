package com.bangpot.crew.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
class CrewController {

	private final CreateCrewUseCase createCrewUseCase;
	private final GetCrewJoinViewUseCase getCrewJoinViewUseCase;
	private final GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;
	private final RequestCrewJoinUseCase requestCrewJoinUseCase;
	private final GetPendingCrewJoinRequestsUseCase getPendingCrewJoinRequestsUseCase;
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
	ResponseEntity<java.util.List<CrewDto.PublicCrewCardResponse>> getPublicCrewCards() {
		return ResponseEntity.ok(CrewDtoMapper.toResponses(getPublicCrewCardsUseCase.handle()));
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
	ResponseEntity<java.util.List<CrewDto.PendingCrewJoinRequestResponse>> getPendingJoinRequests(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewDtoMapper.toPendingResponses(
			getPendingCrewJoinRequestsUseCase.handle(
				CrewDtoMapper.toQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
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
