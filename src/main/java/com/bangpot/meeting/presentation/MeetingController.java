package com.bangpot.meeting.presentation;

import java.util.List;

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
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews/{crewId}/meetings")
class MeetingController {

	private final CreateMeetingUseCase createMeetingUseCase;
	private final GetMeetingsUseCase getMeetingsUseCase;
	private final GetMeetingDetailUseCase getMeetingDetailUseCase;

	@PostMapping
	ResponseEntity<MeetingDto.CreateMeetingResponse> create(
		@PathVariable Long crewId,
		Authentication authentication,
		@Valid @RequestBody MeetingDto.CreateMeetingRequest request
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			createMeetingUseCase.handle(
				MeetingDtoMapper.toCommand(crewId, requireAuthenticatedUserId(authentication), request)
			)
		));
	}

	@GetMapping
	ResponseEntity<List<MeetingDto.MeetingListResponse>> list(
		@PathVariable Long crewId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toListResponses(
			getMeetingsUseCase.handle(
				MeetingDtoMapper.toQuery(crewId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@GetMapping("/{meetingId}")
	ResponseEntity<MeetingDto.MeetingDetailResponse> detail(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			getMeetingDetailUseCase.handle(
				MeetingDtoMapper.toQuery(crewId, meetingId, requireAuthenticatedUserId(authentication))
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
