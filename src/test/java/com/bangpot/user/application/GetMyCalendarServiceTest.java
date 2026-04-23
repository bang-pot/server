package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.domain.User;

class GetMyCalendarServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyCalendarItemsForCompletedUser() {
		userRepository.save(User.create(7L, "bangpot"));
		meetingRepository.putCalendarView(
			7L,
			MyCalendarView.of(
				List.of(
					MyCalendarView.Item.of(
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
					MyCalendarView.Item.of(
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

		MyCalendarView result = getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L));

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
		userRepository.save(User.create(7L, "bangpot"));

		MyCalendarView result = getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L));

		assertThat(result.items()).isEmpty();
		assertThat(result.totalCount()).isZero();
	}

	@Test
	void rejectsCalendarLookupForTempUser() {
		assertThatThrownBy(() -> getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCalendarLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}
}

