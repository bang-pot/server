package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.user.domain.User;

class CompletedUserAccessServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void validatesCompletedUserWhenProfileExists() {
		userRepository.save(User.rehydrate(7L, "bangpot"));

		assertThat(completedUserAccessService.isCompletedUser(7L)).isTrue();
	}

	@Test
	void rejectsIncompleteUserThroughCompletedAccessService() {
		assertThat(completedUserAccessService.isCompletedUser(7L)).isFalse();
		assertThatThrownBy(() -> completedUserAccessService.validateCompletedUser(7L, "가입 완료 사용자만 가능합니다."))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("가입 완료 사용자만 가능합니다.");
	}

	@Test
	void rejectsUnknownUserThroughCompletedAccessService() {
		assertThat(completedUserAccessService.isCompletedUser(999L)).isFalse();
		assertThatThrownBy(() -> completedUserAccessService.validateCompletedUser(999L, "가입 완료 사용자만 가능합니다."))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("가입 완료 사용자만 가능합니다.");
	}
}
