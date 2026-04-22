package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.domain.User;

class GetMyCreatedMeetingsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyCreatedMeetingsForCompletedUser() {
		userRepository.save(User.create(7L, "bangpot"));
		meetingRepository.putCreatedMeetingsView(
			7L,
			MyCreatedMeetingsView.of(
				List.of(
					MyCreatedMeetingsView.Item.of(
						101L,
						"금요일 이스케이프",
						MeetingStatus.COMPLETED,
						"2026-04-17",
						"19:00",
						5L,
						"방탈출 크루"
					)
				),
				MyCreatedMeetingsView.Page.of(0, 20, false)
			)
		);

		MyCreatedMeetingsView result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(101L);
		assertThat(result.items().get(0).title()).isEqualTo("금요일 이스케이프");
		assertThat(result.items().get(0).status()).isEqualTo(MeetingStatus.COMPLETED);
		assertThat(result.items().get(0).date()).isEqualTo("2026-04-17");
		assertThat(result.items().get(0).time()).isEqualTo("19:00");
		assertThat(result.items().get(0).crewId()).isEqualTo(5L);
		assertThat(result.items().get(0).crewName()).isEqualTo("방탈출 크루");
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void defaultsCreatedMeetingListToEmptyWhenNoActivityExists() {
		userRepository.save(User.create(7L, "bangpot"));

		MyCreatedMeetingsView result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void rejectsCreatedMeetingsLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyCreatedMeetingsUseCase.handle(GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
