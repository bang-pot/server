package com.bangpot.explore.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/explore")
class ExploreController {

	private final GetExploreThemesUseCase getExploreThemesUseCase;
	private final GetExploreFiltersUseCase getExploreFiltersUseCase;

	@GetMapping("/themes")
	ResponseEntity<ExploreDto.ExploreThemeListResponse> getThemes(
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
					ExploreDtoMapper.toQuery(keyword, genres, region, district, page, size)
				)
			)
		);
	}

	@GetMapping("/filters")
	ResponseEntity<ExploreDto.ExploreFiltersResponse> getFilters() {
		return ResponseEntity.ok(ExploreDtoMapper.toResponse(getExploreFiltersUseCase.handle()));
	}
}
