package com.bangpot.crew.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crew-invites")
class CrewInviteController {

	private final GetMyCrewInvitesUseCase getMyCrewInvitesUseCase;
	private final AcceptCrewInviteUseCase acceptCrewInviteUseCase;
	private final RejectCrewInviteUseCase rejectCrewInviteUseCase;

	@GetMapping("/me")
	ResponseEntity<List<CrewInviteDto.MyCrewInviteResponse>> getMyInvites(Authentication authentication) {
		return ResponseEntity.ok(CrewInviteDtoMapper.toResponses(
			getMyCrewInvitesUseCase.handle(CrewInviteDtoMapper.toQuery(requireAuthenticatedUserId(authentication)))
		));
	}

	@PostMapping("/{inviteId}/accept")
	ResponseEntity<CrewInviteDto.ProcessCrewInviteResponse> accept(
		@PathVariable Long inviteId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewInviteDtoMapper.toResponse(
			acceptCrewInviteUseCase.handle(
				CrewInviteDtoMapper.toAcceptCommand(inviteId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{inviteId}/reject")
	ResponseEntity<CrewInviteDto.ProcessCrewInviteResponse> reject(
		@PathVariable Long inviteId,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewInviteDtoMapper.toResponse(
			rejectCrewInviteUseCase.handle(
				CrewInviteDtoMapper.toRejectCommand(inviteId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
