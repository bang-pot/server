package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.crew.domain.view.MyPendingCrewsView;
import com.banglog.user.application.usecase.GetMyPendingCrewsUseCase;
import com.banglog.user.domain.User;

class GetMyPendingCrewsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyPendingCrewsForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		crewJoinRequestRepository.putPendingView(
			7L,
			MyPendingCrewsView.of(
				List.of(
					MyPendingCrewsView.Item.of(
						101L,
						11L,
						"Alpha Crew",
						"2026-04-17T09:30:00Z",
						"같이 운동하고 싶습니다"
					)
				),
				MyPendingCrewsView.Page.of(0, 20, false)
			)
		);

		MyPendingCrewsView result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).joinRequestId()).isEqualTo(101L);
		assertThat(result.items().get(0).crewId()).isEqualTo(11L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).requestedAt()).isEqualTo("2026-04-17T09:30:00Z");
		assertThat(result.items().get(0).messageSummary()).isEqualTo("같이 운동하고 싶습니다");
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void defaultsPendingCrewListToEmptyWhenNoPendingRequestsExist() {
		userRepository.save(User.create(7L, "banglog"));

		MyPendingCrewsView result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void rejectsPendingCrewLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyPendingCrewsUseCase.handle(GetMyPendingCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
