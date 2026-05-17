package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.banglog.user.domain.User;
import com.banglog.user.domain.view.MyWithdrawalCheckView;

class GetMyWithdrawalCheckServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsWithdrawalCheckForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		Crew crew = Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null);
		crew.assignId(31L);
		crewRepository.save(crew);
		crewMemberRepository.save(CrewMember.createLeader(31L, 7L));

		MyWithdrawalCheckView result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isFalse();
		assertThat(result.blockingActiveCrews()).hasSize(1);
		assertThat(result.blockingActiveCrews().get(0).crewId()).isEqualTo(31L);
		assertThat(result.blockingActiveCrews().get(0).crewName()).isEqualTo("Alpha Crew");
	}

	@Test
	void allowsWithdrawalWhenNoBlockingReasonExists() {
		userRepository.save(User.create(7L, "banglog"));

		MyWithdrawalCheckView result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isTrue();
		assertThat(result.blockingActiveCrews()).isEmpty();
	}

	@Test
	void rejectsWithdrawalCheckWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
