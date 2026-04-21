package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.PendingCrewReadRepository;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.domain.User;

class GetMyPendingCrewsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyPendingCrewsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));
		pendingCrewReadRepository.putResult(
			7L,
			PendingCrewReadRepository.SearchResult.of(
				List.of(
					PendingCrewReadRepository.Item.of(
						101L,
						11L,
						"Alpha Crew",
						"2026-04-17T09:30:00Z",
						"같이 ?�동?�고 ?�습?�다"
					)
				),
				PendingCrewReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyPendingCrewsUseCase.Result result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).joinRequestId()).isEqualTo(101L);
		assertThat(result.items().get(0).crewId()).isEqualTo(11L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).requestedAt()).isEqualTo("2026-04-17T09:30:00Z");
		assertThat(result.items().get(0).messageSummary()).isEqualTo("같이 ?�동?�고 ?�습?�다");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsPendingCrewListToEmptyWhenNoPendingRequestsExist() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));

		GetMyPendingCrewsUseCase.Result result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsPendingCrewLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyPendingCrewsUseCase.handle(GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsPendingCrewLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyPendingCrewsUseCase.handle(GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsPendingCrewLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyPendingCrewsUseCase.handle(GetMyPendingCrewsUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
