package com.bangpot.crew.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.common.idempotency.Idempotent;
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
	ResponseEntity<CrewInviteDto.MyCrewInvitesResponse> getMyInvites(
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size,
		Authentication authentication
	) {
		return ResponseEntity.ok(CrewInviteDtoMapper.toResponse(
			getMyCrewInvitesUseCase.handle(
				CrewInviteDtoMapper.toQuery(requireAuthenticatedUserId(authentication), page, size)
			)
		));
	}

	@PostMapping("/{inviteId}/accept")
	@Idempotent
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
	@Idempotent
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
