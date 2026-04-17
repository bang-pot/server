package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.CreatedMeetingReadRepository;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.domain.User;

class GetMyCreatedMeetingsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyCreatedMeetingsForCompletedUser() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot", true));
		createdMeetingReadRepository.putResult(
			7L,
			CreatedMeetingReadRepository.SearchResult.of(
				List.of(
					CreatedMeetingReadRepository.Item.of(
						101L,
						"금요일 이스케이프",
						"COMPLETED",
						"2026-04-17",
						"19:00",
						5L,
						"방탈출 크루"
					)
				),
				CreatedMeetingReadRepository.PageInfo.of(0, 20, false)
			)
		);

		GetMyCreatedMeetingsUseCase.Result result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(101L);
		assertThat(result.items().get(0).title()).isEqualTo("금요일 이스케이프");
		assertThat(result.items().get(0).status()).isEqualTo("COMPLETED");
		assertThat(result.items().get(0).date()).isEqualTo("2026-04-17");
		assertThat(result.items().get(0).time()).isEqualTo("19:00");
		assertThat(result.items().get(0).crewId()).isEqualTo(5L);
		assertThat(result.items().get(0).crewName()).isEqualTo("방탈출 크루");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void defaultsCreatedMeetingListToEmptyWhenNoActivityExists() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);
		userRepository.save(User.rehydrate(7L, "bangpot", true));

		GetMyCreatedMeetingsUseCase.Result result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(result.pageInfo().page()).isEqualTo(0);
		assertThat(result.pageInfo().size()).isEqualTo(20);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void rejectsCreatedMeetingsLookupForTempUser() {
		AuthUser authUser = tempUser(7L);
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCreatedMeetingsUseCase.handle(GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCreatedMeetingsLookupWhenUserRowIsMissing() {
		AuthUser authUser = fullUser(7L, "bangpot");
		authUserRepository.save(authUser);

		assertThatThrownBy(() -> getMyCreatedMeetingsUseCase.handle(GetMyCreatedMeetingsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void rejectsCreatedMeetingsLookupWhenAuthUserDoesNotExist() {
		assertThatThrownBy(() -> getMyCreatedMeetingsUseCase.handle(GetMyCreatedMeetingsUseCase.Query.of(77L, 0, 20)))
			.isInstanceOf(AuthUserNotFoundException.class);
	}
}
