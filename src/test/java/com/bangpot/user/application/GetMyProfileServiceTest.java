package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.view.MyProfileView;

class GetMyProfileServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyProfileForCompletedUser() {
		userRepository.save(User.create(7L, "bangpot"));
		meetingRepository.putCounts(7L, 5L, 3L);
		crewRepository.putCounts(7L, 2L, 1L);

		MyProfileView result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L));

		assertThat(result.id()).isEqualTo(7L);
		assertThat(result.nickname()).isEqualTo("bangpot");
		assertThat(result.profileImageUrl()).isNull();
		assertThat(result.createdMeetingsCount()).isEqualTo(5L);
		assertThat(result.joinedMeetingsCount()).isEqualTo(3L);
		assertThat(result.myCrewsCount()).isEqualTo(2L);
		assertThat(result.pendingCrewsCount()).isEqualTo(1L);
	}

	@Test
	void defaultsHubCountsToZeroWhenNoActivityExists() {
		userRepository.save(User.create(7L, "bangpot"));

		MyProfileView result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L));

		assertThat(result.createdMeetingsCount()).isZero();
		assertThat(result.joinedMeetingsCount()).isZero();
		assertThat(result.myCrewsCount()).isZero();
		assertThat(result.pendingCrewsCount()).isZero();
	}

	@Test
	void rejectsMyProfileLookupWhenProfileRowIsMissing() {
		assertThatThrownBy(() -> getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("프로필 완료가 필요합니다.");
	}
}
