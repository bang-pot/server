package com.bangpot.meeting.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.meeting.application.usecase.GetCrewMeetingHistoryUseCase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews/{crewId}/history/meetings")
class MeetingHistoryController {

	private final GetCrewMeetingHistoryUseCase getCrewMeetingHistoryUseCase;

	@GetMapping
	ResponseEntity<MeetingHistoryDto.MeetingHistoryListResponse> list(
		@PathVariable Long crewId,
		Authentication authentication,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(MeetingHistoryDtoMapper.toResponse(
			getCrewMeetingHistoryUseCase.handle(
				GetCrewMeetingHistoryUseCase.Query.of(crewId, requireAuthenticatedUserId(authentication), page, size)
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
