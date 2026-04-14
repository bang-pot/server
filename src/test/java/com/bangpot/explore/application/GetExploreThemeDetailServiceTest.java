package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.service.GetExploreThemeDetailService;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemeDetailUseCase;

class GetExploreThemeDetailServiceTest {

	private InMemoryExploreThemeReadRepository repository;
	private GetExploreThemeDetailUseCase useCase;

	@BeforeEach
	void setUp() {
		repository = new InMemoryExploreThemeReadRepository();
		useCase = new GetExploreThemeDetailService(repository);
	}

	@Test
	void returnsThemeDetailWithRelatedThemes() {
		repository.detail = ExploreThemeReadRepository.ThemeDetail.of(
			5L,
			"Deep Blue",
			1L,
			"Seoul Escape Hongdae",
			"Seoul Mapo",
			"HORROR",
			"https://image.example/deep-blue.jpg",
			4,
			60,
			"Deep sea mystery theme",
			"https://example.com/deep-blue",
			List.of(
				ExploreThemeReadRepository.RelatedThemeSummary.of(
					1L,
					"Laugh Track",
					1L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					"COMEDY",
					null,
					2,
					50
				),
				ExploreThemeReadRepository.RelatedThemeSummary.of(
					3L,
					"Time Attack",
					1L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					"THRILLER",
					null,
					3,
					75
				)
			)
		);

		GetExploreThemeDetailUseCase.Result result = useCase.handle(GetExploreThemeDetailUseCase.Query.of(5L));

		assertThat(result.themeId()).isEqualTo(5L);
		assertThat(result.themeName()).isEqualTo("Deep Blue");
		assertThat(result.relatedThemes()).hasSize(2);
		assertThat(result.relatedThemes())
			.extracting(GetExploreThemeDetailUseCase.RelatedTheme::themeName)
			.containsExactly("Laugh Track", "Time Attack");
	}

	@Test
	void throwsWhenThemeDoesNotExist() {
		assertThatThrownBy(() -> useCase.handle(GetExploreThemeDetailUseCase.Query.of(999L)))
			.isInstanceOf(ExploreThemeNotFoundException.class);
	}

	private static final class InMemoryExploreThemeReadRepository implements ExploreThemeReadRepository {

		private ThemeDetail detail;

		@Override
		public SearchResult search(Condition condition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public GetExploreFiltersUseCase.Result getFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<ThemeDetail> getThemeDetail(Long themeId) {
			if (detail == null || !detail.themeId().equals(themeId)) {
				return Optional.empty();
			}
			return Optional.of(detail);
		}
	}
}
