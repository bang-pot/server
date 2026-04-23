package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.view.UserSearchView;

class SearchUsersServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMatchingUsersForCompletedUser() {
		userRepository.save(User.create(7L, "bangpot"));
		userRepository.putSearchResult(
			"pot",
			List.of(
				UserSearchView.Item.of(
					11L,
					"alpha-pot",
					"https://cdn.example.com/users/11.jpg",
					"love escape rooms",
					"FEMALE",
					0
				),
				UserSearchView.Item.of(
					12L,
					"bangpot",
					null,
					null,
					null,
					0
				)
			)
		);
		meetingQueryRepository.putCompletedCount(11L, 3);

		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, " pot ", 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 20, 2L, 1));
		assertThat(result.items().get(0).userId()).isEqualTo(11L);
		assertThat(result.items().get(0).nickname()).isEqualTo("alpha-pot");
		assertThat(result.items().get(0).profileImageUrl()).isEqualTo("https://cdn.example.com/users/11.jpg");
		assertThat(result.items().get(0).bio()).isEqualTo("love escape rooms");
		assertThat(result.items().get(0).gender()).isEqualTo("FEMALE");
		assertThat(result.items().get(0).escapeCount()).isEqualTo(3);
		assertThat(result.items().get(1).userId()).isEqualTo(12L);
	}

	@Test
	void returnsEmptyResultForBlankKeywordWithoutQueryingRepository() {
		userRepository.save(User.create(7L, "bangpot"));

		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, "   ", 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 20, 0L, 0));
		assertThat(userRepository.getSearchCount()).isZero();
	}

	@Test
	void rejectsSearchWhenUserRowIsMissing() {
		assertThatThrownBy(() -> searchUsersUseCase.handle(SearchUsersUseCase.Query.of(7L, "pot", 0, 20)))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("프로필 완료가 필요합니다.");
	}

	@Test
	void returnsEmptyResultWhenSearchResultDoesNotExist() {
		userRepository.save(User.create(7L, "bangpot"));

		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, "pot", 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 20, 0L, 0));
	}

	@Test
	void clampsRequestedSizeIntoAllowedRange() {
		userRepository.save(User.create(7L, "bangpot"));
		userRepository.putSearchResult(
			"pot",
			List.of(
				UserSearchView.Item.of(11L, "alpha-pot", null, null, null, 0),
				UserSearchView.Item.of(12L, "beta-pot", null, null, null, 0)
			)
		);

		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, "pot", 0, 0)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(0, 1, 2L, 2));
		assertThat(userRepository.getLastSearchPage()).isEqualTo(0);
		assertThat(userRepository.getLastSearchSize()).isEqualTo(1);
	}

	@Test
	void returnsRequestedPageSlice() {
		userRepository.save(User.create(7L, "bangpot"));
		userRepository.putSearchResult(
			"pot",
			List.of(
				UserSearchView.Item.of(11L, "alpha-pot", null, null, null, 0),
				UserSearchView.Item.of(12L, "beta-pot", null, null, null, 0),
				UserSearchView.Item.of(13L, "gamma-pot", null, null, null, 0)
			)
		);

		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, "pot", 1, 1)
		);

		assertThat(result.items()).extracting(UserSearchView.Item::userId)
			.containsExactly(12L);
		assertThat(result.page()).isEqualTo(UserSearchView.Page.of(1, 1, 3L, 3));
	}
}
