package com.banglog.meeting.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banglog.auth.presentation.UnauthenticatedException;
import com.banglog.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.banglog.meeting.application.usecase.GetCrewMeetingGalleryUseCase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
class MeetingGalleryController {

	private final GetCrewMeetingGalleryUseCase getCrewMeetingGalleryUseCase;
	private final GetCrewMeetingGalleryDetailUseCase getCrewMeetingGalleryDetailUseCase;

	@GetMapping("/{crewId}/gallery")
	ResponseEntity<MeetingGalleryDto.MeetingGalleryResponse> getGallery(
		@PathVariable Long crewId,
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return ResponseEntity.ok(
			MeetingGalleryDtoMapper.toResponse(
				getCrewMeetingGalleryUseCase.handle(
					GetCrewMeetingGalleryUseCase.Query.of(crewId, requireAuthenticatedUserId(authentication), page, size)
				)
			)
		);
	}

	@GetMapping("/{crewId}/gallery/{meetingId}")
	ResponseEntity<MeetingGalleryDto.MeetingGalleryDetailResponse> getGalleryDetail(
		@PathVariable Long crewId,
		@PathVariable Long meetingId,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			MeetingGalleryDtoMapper.toResponse(
				getCrewMeetingGalleryDetailUseCase.handle(
					GetCrewMeetingGalleryDetailUseCase.Query.of(crewId, meetingId, requireAuthenticatedUserId(authentication))
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
