package com.bangpot.gallerylog.presentation;

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
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.gallerylog.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.UpdateMeetingLogUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
class GalleryLogController {

	private final CreateMeetingLogUseCase createMeetingLogUseCase;
	private final UpdateMeetingLogUseCase updateMeetingLogUseCase;
	private final DeleteMeetingLogUseCase deleteMeetingLogUseCase;
	private final GetMyMeetingLogUseCase getMyMeetingLogUseCase;
	private final GetMeetingLogDetailUseCase getMeetingLogDetailUseCase;

	@PostMapping("/meetings/{meetingId}/logs")
	ResponseEntity<GalleryLogDto.MeetingLogWriteResponse> create(
		@PathVariable Long meetingId,
		Authentication authentication,
		@Valid @RequestBody GalleryLogDto.CreateMeetingLogRequest request
	) {
		return ResponseEntity.ok(
			GalleryLogDtoMapper.toResponse(
				createMeetingLogUseCase.handle(
					GalleryLogDtoMapper.toCommand(meetingId, requireAuthenticatedUserId(authentication), request)
				)
			)
		);
	}

	@PatchMapping("/logs/{logId}")
	ResponseEntity<GalleryLogDto.MeetingLogWriteResponse> update(
		@PathVariable Long logId,
		Authentication authentication,
		@Valid @RequestBody GalleryLogDto.UpdateMeetingLogRequest request
	) {
		return ResponseEntity.ok(
			GalleryLogDtoMapper.toResponse(
				updateMeetingLogUseCase.handle(
					GalleryLogDtoMapper.toCommand(logId, requireAuthenticatedUserId(authentication), request)
				)
			)
		);
	}

	@DeleteMapping("/logs/{logId}")
	ResponseEntity<GalleryLogDto.MeetingLogDeleteResponse> delete(
		@PathVariable Long logId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			GalleryLogDtoMapper.toResponse(
				deleteMeetingLogUseCase.handle(
					GalleryLogDtoMapper.toCommand(logId, requireAuthenticatedUserId(authentication))
				)
			)
		);
	}

	@GetMapping("/meetings/{meetingId}/logs/me")
	ResponseEntity<GalleryLogDto.MeetingLogDetailResponse> getMyLog(
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			GalleryLogDtoMapper.toResponse(
				getMyMeetingLogUseCase.handle(
					GalleryLogDtoMapper.toQuery(meetingId, requireAuthenticatedUserId(authentication))
				)
			)
		);
	}

	@GetMapping("/logs/{logId}")
	ResponseEntity<GalleryLogDto.MeetingLogDetailResponse> getDetail(
		@PathVariable Long logId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			GalleryLogDtoMapper.toResponse(
				getMeetingLogDetailUseCase.handle(
					GalleryLogDtoMapper.toDetailQuery(logId, requireAuthenticatedUserId(authentication))
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
