package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.CalendarReadRepository;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.domain.User;

class GetMyCalendarServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyCalendarItemsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));
		calendarReadRepository.putView(
			7L,
			CalendarReadRepository.View.of(
				List.of(
					CalendarReadRepository.Item.of(
						301L,
						"Friday Escape",
						5L,
						"Room Escape Crew",
						"2026-04-20",
						"19:00",
						"RECRUITING",
						false,
						"HOST"
					),
					CalendarReadRepository.Item.of(
						302L,
						"Canceled Escape",
						6L,
						"Another Crew",
						"2026-04-21",
						"20:00",
						"CANCELED",
						true,
						"PARTICIPANT"
					)
				),
				2
			)
		);

		GetMyCalendarUseCase.Result result = getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L));

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0).meetingId()).isEqualTo(301L);
		assertThat(result.items().get(0).meetingTitle()).isEqualTo("Friday Escape");
		assertThat(result.items().get(0).crewId()).isEqualTo(5L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Room Escape Crew");
		assertThat(result.items().get(0).date()).isEqualTo("2026-04-20");
		assertThat(result.items().get(0).time()).isEqualTo("19:00");
		assertThat(result.items().get(0).meetingStatus()).isEqualTo("RECRUITING");
		assertThat(result.items().get(0).isCanceled()).isFalse();
		assertThat(result.items().get(0).participationRole()).isEqualTo("HOST");
		assertThat(result.items().get(1).isCanceled()).isTrue();
		assertThat(result.totalCount()).isEqualTo(2);
	}

	@Test
	void defaultsCalendarToEmptyWhenNoActivityExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot"));

		GetMyCalendarUseCase.Result result = getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L));

		assertThat(result.items()).isEmpty();
		assertThat(result.totalCount()).isZero();
	}

	@Test
	void rejectsCalendarLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCalendarLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsCalendarLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(77L)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
