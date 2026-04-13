package com.bangpot.meeting.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.bangpot.meeting.application.usecase.CompleteMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;

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
	private final JoinMeetingUseCase joinMeetingUseCase;
	private final CancelMeetingParticipationUseCase cancelMeetingParticipationUseCase;
	private final CloseMeetingRecruitmentUseCase closeMeetingRecruitmentUseCase;
	private final ReopenMeetingRecruitmentUseCase reopenMeetingRecruitmentUseCase;
	private final CancelMeetingUseCase cancelMeetingUseCase;
	private final CompleteMeetingUseCase completeMeetingUseCase;

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

	@PostMapping("/{meetingId}/join")
	ResponseEntity<MeetingDto.MeetingJoinResponse> join(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			joinMeetingUseCase.handle(
				MeetingDtoMapper.toCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@DeleteMapping("/{meetingId}/join")
	ResponseEntity<MeetingDto.MeetingJoinResponse> cancelJoin(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			cancelMeetingParticipationUseCase.handle(
				MeetingDtoMapper.toCancelCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{meetingId}/close-recruitment")
	ResponseEntity<MeetingDto.MeetingStatusChangeResponse> closeRecruitment(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			closeMeetingRecruitmentUseCase.handle(
				MeetingDtoMapper.toCloseRecruitmentCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{meetingId}/reopen-recruitment")
	ResponseEntity<MeetingDto.MeetingStatusChangeResponse> reopenRecruitment(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			reopenMeetingRecruitmentUseCase.handle(
				MeetingDtoMapper.toReopenRecruitmentCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{meetingId}/cancel")
	ResponseEntity<MeetingDto.MeetingStatusChangeResponse> cancelMeeting(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			cancelMeetingUseCase.handle(
				MeetingDtoMapper.toCancelMeetingCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
			)
		));
	}

	@PostMapping("/{meetingId}/complete")
	ResponseEntity<MeetingDto.MeetingStatusChangeResponse> completeMeeting(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			completeMeetingUseCase.handle(
				MeetingDtoMapper.toCompleteMeetingCommand(crewId, meetingId, requireAuthenticatedUserId(authentication))
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
