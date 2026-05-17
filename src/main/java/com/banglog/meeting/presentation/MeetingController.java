package com.banglog.meeting.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banglog.auth.presentation.UnauthenticatedException;
import com.banglog.common.idempotency.Idempotent;
import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingUseCase;
import com.banglog.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.CompleteMeetingUseCase;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.application.usecase.RecordMeetingResultUseCase;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews/{crewId}/meetings")
class MeetingController {

	private final CreateMeetingUseCase createMeetingUseCase;
	private final UpdateMeetingUseCase updateMeetingUseCase;
	private final GetMeetingsUseCase getMeetingsUseCase;
	private final GetMeetingDetailUseCase getMeetingDetailUseCase;
	private final JoinMeetingUseCase joinMeetingUseCase;
	private final CancelMeetingParticipationUseCase cancelMeetingParticipationUseCase;
	private final CloseMeetingRecruitmentUseCase closeMeetingRecruitmentUseCase;
	private final ReopenMeetingRecruitmentUseCase reopenMeetingRecruitmentUseCase;
	private final CancelMeetingUseCase cancelMeetingUseCase;
	private final CompleteMeetingUseCase completeMeetingUseCase;
	private final RecordMeetingResultUseCase recordMeetingResultUseCase;

	@PostMapping
	@Idempotent
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

	@PatchMapping("/{meetingId}")
	@Idempotent
	ResponseEntity<MeetingDto.UpdateMeetingResponse> update(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication,
		@Valid @RequestBody MeetingDto.UpdateMeetingRequest request
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			updateMeetingUseCase.handle(
				MeetingDtoMapper.toCommand(crewId, meetingId, requireAuthenticatedUserId(authentication), request)
			)
		));
	}

	@GetMapping
	ResponseEntity<MeetingDto.MeetingListPageResponse> list(
		@PathVariable Long crewId,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size,
		Authentication authentication
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toListResponse(
			getMeetingsUseCase.handle(
				MeetingDtoMapper.toQuery(crewId, requireAuthenticatedUserId(authentication), page, size)
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
	@Idempotent
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
	@Idempotent
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
	@Idempotent
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
	@Idempotent
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
	@Idempotent
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
	@Idempotent
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

	@PostMapping("/{meetingId}/result")
	@Idempotent
	ResponseEntity<MeetingDto.MeetingResultRecordResponse> recordResult(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication,
		@Valid @RequestBody MeetingDto.RecordMeetingResultRequest request
	) {
		return ResponseEntity.ok(MeetingDtoMapper.toResponse(
			recordMeetingResultUseCase.handle(
				MeetingDtoMapper.toRecordResultCommand(
					crewId,
					meetingId,
					requireAuthenticatedUserId(authentication),
					request
				)
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
