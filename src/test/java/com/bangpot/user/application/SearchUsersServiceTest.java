package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserSearchReadRepository;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.domain.User;

class SearchUsersServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMatchingUsersForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		userSearchReadRepository.putResult(
			"pot",
			List.of(
				UserSearchReadRepository.Item.of(
					11L,
					"alpha-pot",
					"https://cdn.example.com/users/11.jpg",
					"love escape rooms",
					"FEMALE",
					0
				),
				UserSearchReadRepository.Item.of(
					12L,
					"bangpot",
					null,
					null,
					null,
					0
				)
			)
		);

		SearchUsersUseCase.Result result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, " pot ", 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0).userId()).isEqualTo(11L);
		assertThat(result.items().get(0).nickname()).isEqualTo("alpha-pot");
		assertThat(result.items().get(0).profileImageUrl()).isEqualTo("https://cdn.example.com/users/11.jpg");
		assertThat(result.items().get(0).bio()).isEqualTo("love escape rooms");
		assertThat(result.items().get(0).gender()).isEqualTo("FEMALE");
		assertThat(result.items().get(0).escapeCount()).isZero();
		assertThat(result.items().get(1).userId()).isEqualTo(12L);
	}

	@Test
	void returnsEmptyResultForBlankKeywordWithoutQueryingRepository() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		SearchUsersUseCase.Result result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(7L, "   ", 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(userSearchReadRepository.getSearchCount()).isZero();
	}

	@Test
	void rejectsSearchForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> searchUsersUseCase.handle(SearchUsersUseCase.Query.of(7L, "pot", 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsSearchWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> searchUsersUseCase.handle(SearchUsersUseCase.Query.of(7L, "pot", 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsSearchWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> searchUsersUseCase.handle(SearchUsersUseCase.Query.of(77L, "pot", 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
