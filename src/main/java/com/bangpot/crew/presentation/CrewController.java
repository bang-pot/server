package com.bangpot.crew.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.domain.CrewRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
class CrewController {

	private final CreateCrewUseCase createCrewUseCase;

	@PostMapping
	ResponseEntity<CreateCrewResponse> create(
		Authentication authentication,
		@Valid @RequestBody CreateCrewRequest request
	) {
		CreateCrewUseCase.Result result = createCrewUseCase.handle(CreateCrewUseCase.Command.of(
			requireAuthenticatedUserId(authentication),
			request.name(),
			request.description(),
			request.visibility(),
			request.imageUrl()
		));
		return ResponseEntity.ok(new CreateCrewResponse(result.crewId(), result.name(), result.myRole()));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}

	record CreateCrewRequest(
		@NotBlank(message = "크루명은 비어 있을 수 없습니다.") String name,
		String description,
		String visibility,
		String imageUrl
	) {
	}

	record CreateCrewResponse(Long crewId, String name, CrewRole myRole) {
	}
}
