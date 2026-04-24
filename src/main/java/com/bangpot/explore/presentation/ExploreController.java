package com.bangpot.explore.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/explore")
class ExploreController {

	private final GetExploreThemesUseCase getExploreThemesUseCase;
	private final GetExploreFiltersUseCase getExploreFiltersUseCase;
	private final GetExploreThemeDetailUseCase getExploreThemeDetailUseCase;

	@GetMapping("/themes")
	ResponseEntity<ExploreDto.ExploreThemeListResponse> getThemes(
		Authentication authentication,
		@RequestParam(value = "q", required = false) String keyword,
		@RequestParam(value = "genres", required = false) List<String> genres,
		@RequestParam(value = "region", required = false) String region,
		@RequestParam(value = "district", required = false) String district,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				getExploreThemesUseCase.handle(
					ExploreDtoMapper.toQuery(authenticatedUserIdOrNull(authentication), keyword, genres, region, district, page, size)
				)
			)
		);
	}

	@GetMapping("/themes/{themeId}")
	ResponseEntity<ExploreDto.ExploreThemeDetailResponse> getThemeDetail(
		Authentication authentication,
		@PathVariable("themeId") @Positive(message = "themeId는 1 이상이어야 합니다.") Long themeId
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				getExploreThemeDetailUseCase.handle(
					GetExploreThemeDetailUseCase.Query.of(authenticatedUserIdOrNull(authentication), themeId)
				)
			)
		);
	}

	@GetMapping("/filters")
	ResponseEntity<ExploreDto.ExploreFiltersResponse> getFilters() {
		return ResponseEntity.ok(ExploreDtoMapper.toResponse(getExploreFiltersUseCase.handle()));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}

	private Long authenticatedUserIdOrNull(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			return null;
		}
		return userId;
	}
}
