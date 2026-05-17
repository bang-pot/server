package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.user.domain.User;

class CompletedUserAccessServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void validatesCompletedUserWhenProfileExists() {
		userRepository.save(User.create(7L, "banglog"));

		assertThat(completedUserAccessService.isCompletedUser(7L)).isTrue();
	}

	@Test
	void rejectsMissingProfileThroughCompletedAccessService() {
		assertThat(completedUserAccessService.isCompletedUser(7L)).isFalse();
		assertThatThrownBy(() -> completedUserAccessService.validateCompletedUser(7L, "프로필 완료가 필요합니다."))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("프로필 완료가 필요합니다.");
	}

	@Test
	void rejectsUnknownUserThroughCompletedAccessService() {
		assertThat(completedUserAccessService.isCompletedUser(999L)).isFalse();
		assertThatThrownBy(() -> completedUserAccessService.validateCompletedUser(999L, "프로필 완료가 필요합니다."))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("프로필 완료가 필요합니다.");
	}
}
