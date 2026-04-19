package com.bangpot.home.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.home.application.usecase.GetHomeUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class HomeController {

	private final GetHomeUseCase getHomeUseCase;

	@GetMapping("/api/home")
	ResponseEntity<HomeDto.HomeResponse> getHome(Authentication authentication) {
		return ResponseEntity.ok(
			HomeDtoMapper.toResponse(
				getHomeUseCase.handle(GetHomeUseCase.Query.of(optionalAuthenticatedUserId(authentication)))
			)
		);
	}

	private Long optionalAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			return null;
		}
		return userId;
	}
}
