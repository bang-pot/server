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
import com.bangpot.explore.application.usecase.GetExploreMeetingCreateCrewsUseCase;
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
	private final GetExploreMeetingCreateCrewsUseCase getExploreMeetingCreateCrewsUseCase;

	@GetMapping("/themes")
	ResponseEntity<ExploreDto.ExploreThemeListResponse> getThemes(
		@RequestParam(value = "q", required = false) String keyword,
		@RequestParam(value = "genres", required = false) List<String> genres,
		@RequestParam(value = "region", required = false) String region,
		@RequestParam(value = "district", required = false) String district,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page??0 ?댁긽?댁뼱???⑸땲??") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size??1 ?댁긽?댁뼱???⑸땲??") @Max(value = 50, message = "size??50 ?댄븯?ъ빞 ?⑸땲??") int size
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				getExploreThemesUseCase.handle(
					ExploreDtoMapper.toQuery(keyword, genres, region, district, page, size)
				)
			)
		);
	}

	@GetMapping("/themes/{themeId}")
	ResponseEntity<ExploreDto.ExploreThemeDetailResponse> getThemeDetail(
		@PathVariable("themeId") @Positive(message = "themeId??1 ?댁긽?댁뼱???⑸땲??") Long themeId
	) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(getExploreThemeDetailUseCase.handle(GetExploreThemeDetailUseCase.Query.of(themeId)))
		);
	}

	@GetMapping("/meeting-create/crews")
	ResponseEntity<ExploreDto.ExploreMeetingCreateCrewsResponse> getMeetingCreateCrews(Authentication authentication) {
		return ResponseEntity.ok(
			ExploreDtoMapper.toResponse(
				getExploreMeetingCreateCrewsUseCase.handle(
					GetExploreMeetingCreateCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication))
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
}
