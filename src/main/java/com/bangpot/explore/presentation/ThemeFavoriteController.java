package com.bangpot.explore.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.explore.application.usecase.AddThemeFavoriteUseCase;
import com.bangpot.explore.application.usecase.RemoveThemeFavoriteUseCase;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/themes")
class ThemeFavoriteController {

	private final AddThemeFavoriteUseCase addThemeFavoriteUseCase;
	private final RemoveThemeFavoriteUseCase removeThemeFavoriteUseCase;

	@PostMapping("/{themeId}/favorite")
	ResponseEntity<ExploreDto.ThemeFavoriteResponse> addFavorite(
		Authentication authentication,
		@PathVariable("themeId") @Positive(message = "themeId는 1 이상이어야 합니다.") Long themeId
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				addThemeFavoriteUseCase.handle(AddThemeFavoriteUseCase.Command.of(requireAuthenticatedUserId(authentication), themeId))
			)
		);
	}

	@DeleteMapping("/{themeId}/favorite")
	ResponseEntity<ExploreDto.ThemeFavoriteResponse> removeFavorite(
		Authentication authentication,
		@PathVariable("themeId") @Positive(message = "themeId는 1 이상이어야 합니다.") Long themeId
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				removeThemeFavoriteUseCase.handle(
					RemoveThemeFavoriteUseCase.Command.of(requireAuthenticatedUserId(authentication), themeId)
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
