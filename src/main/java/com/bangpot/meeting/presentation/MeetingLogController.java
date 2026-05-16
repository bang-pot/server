package com.bangpot.meeting.presentation;

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
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.common.idempotency.Idempotent;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
class MeetingLogController {

	private final CreateMeetingLogUseCase createMeetingLogUseCase;
	private final UpdateMeetingLogUseCase updateMeetingLogUseCase;
	private final DeleteMeetingLogUseCase deleteMeetingLogUseCase;
	private final GetMyMeetingLogUseCase getMyMeetingLogUseCase;
	private final GetMeetingLogDetailUseCase getMeetingLogDetailUseCase;
	private final UploadMeetingLogPhotoUseCase uploadMeetingLogPhotoUseCase;

	@PostMapping("/meetings/{meetingId}/logs")
	@Idempotent
	ResponseEntity<MeetingLogDto.MeetingLogWriteResponse> create(
		@PathVariable Long meetingId,
		Authentication authentication,
		@Valid @RequestBody MeetingLogDto.CreateMeetingLogRequest request
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				createMeetingLogUseCase.handle(
					MeetingLogDtoMapper.toCommand(meetingId, requireAuthenticatedUserId(authentication), request)
				)
			)
		);
	}

	@PatchMapping("/logs/{logId}")
	@Idempotent
	ResponseEntity<MeetingLogDto.MeetingLogWriteResponse> update(
		@PathVariable Long logId,
		Authentication authentication,
		@Valid @RequestBody MeetingLogDto.UpdateMeetingLogRequest request
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				updateMeetingLogUseCase.handle(
					MeetingLogDtoMapper.toCommand(logId, requireAuthenticatedUserId(authentication), request)
				)
			)
		);
	}

	@DeleteMapping("/crews/{crewId}/logs/{logId}")
	@Idempotent
	ResponseEntity<MeetingLogDto.MeetingLogDeleteResponse> delete(
		@PathVariable Long crewId,
		@PathVariable Long logId,
		Authentication authentication,
		@RequestBody(required = false) MeetingLogDto.DeleteMeetingLogRequest request
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				deleteMeetingLogUseCase.handle(
					MeetingLogDtoMapper.toDeleteCommand(
						crewId,
						logId,
						requireAuthenticatedUserId(authentication),
						request
					)
				)
			)
		);
	}

	@GetMapping("/meetings/{meetingId}/logs/me")
	ResponseEntity<MeetingLogDto.MyMeetingLogResponse> getMyLog(
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				getMyMeetingLogUseCase.handle(
					MeetingLogDtoMapper.toQuery(meetingId, requireAuthenticatedUserId(authentication))
				)
			)
		);
	}

	@GetMapping("/crews/{crewId}/logs/{logId}")
	ResponseEntity<MeetingLogDto.MeetingLogDetailResponse> getDetail(
		@PathVariable Long crewId,
		@PathVariable Long logId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				getMeetingLogDetailUseCase.handle(
					MeetingLogDtoMapper.toDetailQuery(crewId, logId, requireAuthenticatedUserId(authentication))
				)
			)
		);
	}

	@PostMapping("/meetings/{meetingId}/logs/photos")
	ResponseEntity<MeetingLogDto.MeetingLogPhotoUploadResponse> uploadPhoto(
		@PathVariable Long meetingId,
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		return ResponseEntity.ok(
			MeetingLogDtoMapper.toResponse(
				uploadMeetingLogPhotoUseCase.handle(
					UploadMeetingLogPhotoUseCase.Command.of(
						meetingId,
						requireAuthenticatedUserId(authentication),
						file
					)
				)
			)
		);
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
