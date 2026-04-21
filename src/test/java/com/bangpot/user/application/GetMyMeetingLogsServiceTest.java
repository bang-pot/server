package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.MyMeetingLogReadRepository;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;
import com.bangpot.user.domain.User;

class GetMyMeetingLogsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyMeetingLogsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));
		myMeetingLogReadRepository.putResult(
			7L,
			MyMeetingLogReadRepository.SearchResult.of(
				List.of(
					MyMeetingLogReadRepository.Item.of(
						501L,
						31L,
						"Alpha Crew",
						201L,
						"Friday Escape",
						"2026-04-18",
						Instant.parse("2026-04-19T10:15:30Z"),
						"1234567890".repeat(13),
						"https://cdn.example.com/log-cover.jpg",
						3L
					)
				),
				MyMeetingLogReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyMeetingLogsUseCase.Result result = getMyMeetingLogsUseCase.handle(
			GetMyMeetingLogsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).logId()).isEqualTo(501L);
		assertThat(result.items().get(0).crewId()).isEqualTo(31L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).meetingId()).isEqualTo(201L);
		assertThat(result.items().get(0).meetingTitle()).isEqualTo("Friday Escape");
		assertThat(result.items().get(0).meetingDate()).isEqualTo("2026-04-18");
		assertThat(result.items().get(0).createdAt()).isEqualTo(Instant.parse("2026-04-19T10:15:30Z"));
		assertThat(result.items().get(0).excerpt()).hasSize(120);
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/log-cover.jpg");
		assertThat(result.items().get(0).photoCount()).isEqualTo(3L);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsMeetingLogListToEmptyWhenNoAuthoredLogsExist() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));

		GetMyMeetingLogsUseCase.Result result = getMyMeetingLogsUseCase.handle(
			GetMyMeetingLogsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsMeetingLogLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyMeetingLogsUseCase.handle(GetMyMeetingLogsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingLogLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyMeetingLogsUseCase.handle(GetMyMeetingLogsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsMeetingLogLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyMeetingLogsUseCase.handle(GetMyMeetingLogsUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
