package com.bangpot.meeting.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.meeting.application.usecase.GetArchivedMeetingsUseCase;
import com.bangpot.auth.presentation.UnauthenticatedException;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/archive")
class ArchiveController {

	private final GetArchivedMeetingsUseCase getArchivedMeetingsUseCase;

	@GetMapping("/meetings")
	ResponseEntity<ArchiveDto.ArchiveMeetingsResponse> getMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return ResponseEntity.ok(
			ArchiveDtoMapper.toResponse(
				getArchivedMeetingsUseCase.handle(
					ArchiveDtoMapper.toQuery(requireAuthenticatedUserId(authentication), page, size)
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

