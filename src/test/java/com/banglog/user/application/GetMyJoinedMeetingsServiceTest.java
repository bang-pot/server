package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.banglog.user.domain.User;

class GetMyJoinedMeetingsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyJoinedMeetingsForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		meetingRepository.putJoinedMeetingsView(
			7L,
			MyJoinedMeetingsView.of(
				List.of(
					MyJoinedMeetingsView.Item.of(
						201L,
						"일요일 방탈출",
						"Deep Blue",
						5L,
						"방탈출 크루",
						"2026-04-18",
						"19:00",
						MeetingStatus.COMPLETED,
						true
					)
				),
				MyJoinedMeetingsView.Page.of(0, 20, false)
			)
		);

		MyJoinedMeetingsView result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(201L);
		assertThat(result.items().get(0).title()).isEqualTo("일요일 방탈출");
		assertThat(result.items().get(0).themeName()).isEqualTo("Deep Blue");
		assertThat(result.items().get(0).crewId()).isEqualTo(5L);
		assertThat(result.items().get(0).crewName()).isEqualTo("방탈출 크루");
		assertThat(result.items().get(0).date()).isEqualTo("2026-04-18");
		assertThat(result.items().get(0).time()).isEqualTo("19:00");
		assertThat(result.items().get(0).status()).isEqualTo(MeetingStatus.COMPLETED);
		assertThat(result.items().get(0).canWriteReview()).isTrue();
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void defaultsJoinedMeetingListToEmptyWhenNoActivityExists() {
		userRepository.save(User.create(7L, "banglog"));

		MyJoinedMeetingsView result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void rejectsJoinedMeetingsLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
