package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.MyCrewReadRepository;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.domain.User;

class GetMyCrewsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyActiveCrewsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		myCrewReadRepository.putResult(
			7L,
			MyCrewReadRepository.SearchResult.of(
				List.of(
					MyCrewReadRepository.Item.of(
						11L,
						"Alpha Crew",
						"PUBLIC",
						"leader-pot",
						"https://cdn.example.com/crew-alpha.jpg"
					)
				),
				MyCrewReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyCrewsUseCase.Result result = getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20));

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).crewId()).isEqualTo(11L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).visibility()).isEqualTo("PUBLIC");
		assertThat(result.items().get(0).leaderNickname()).isEqualTo("leader-pot");
		assertThat(result.items().get(0).coverImageUrl()).isEqualTo("https://cdn.example.com/crew-alpha.jpg");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsCrewListToEmptyWhenNoActiveMembershipExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		GetMyCrewsUseCase.Result result = getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20));

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsMyCrewsLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMyCrewsLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsMyCrewsLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
