package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.JoinedMeetingReadRepository;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.domain.User;

class GetMyJoinedMeetingsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyJoinedMeetingsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));
		joinedMeetingReadRepository.putResult(
			7L,
			JoinedMeetingReadRepository.SearchResult.of(
				List.of(
					JoinedMeetingReadRepository.Item.of(
						201L,
						"토요일 방탈",
						"Deep Blue",
						5L,
						"방탈출 크루",
						"2026-04-18",
						"19:00",
						"COMPLETED",
						"SUCCESS",
						true
					)
				),
				JoinedMeetingReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyJoinedMeetingsUseCase.Result result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(201L);
		assertThat(result.items().get(0).title()).isEqualTo("토요일 방탈");
		assertThat(result.items().get(0).themeName()).isEqualTo("Deep Blue");
		assertThat(result.items().get(0).crewId()).isEqualTo(5L);
		assertThat(result.items().get(0).crewName()).isEqualTo("방탈출 크루");
		assertThat(result.items().get(0).date()).isEqualTo("2026-04-18");
		assertThat(result.items().get(0).time()).isEqualTo("19:00");
		assertThat(result.items().get(0).status()).isEqualTo("COMPLETED");
		assertThat(result.items().get(0).result()).isEqualTo("SUCCESS");
		assertThat(result.items().get(0).canWriteReview()).isTrue();
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsJoinedMeetingListToEmptyWhenNoActivityExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.create(7L, "bangpot"));

		GetMyJoinedMeetingsUseCase.Result result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsJoinedMeetingsLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsJoinedMeetingsLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsJoinedMeetingsLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
