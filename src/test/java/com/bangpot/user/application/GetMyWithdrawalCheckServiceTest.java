package com.bangpot.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.domain.User;

class GetMyWithdrawalCheckServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsWithdrawalCheckForCompletedUser() {
		userRepository.save(User.create(7L, "bangpot"));
		Crew crew = Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null);
		crew.assignId(31L);
		crewRepository.save(crew);
		crewMemberRepository.save(CrewMember.createLeader(31L, 7L));

		GetMyWithdrawalCheckUseCase.Result result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isFalse();
		assertThat(result.blockingActiveCrews()).hasSize(1);
		assertThat(result.blockingActiveCrews().get(0).crewId()).isEqualTo(31L);
		assertThat(result.blockingActiveCrews().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.blockingParticipatingMeetings()).isEmpty();
	}

	@Test
	void allowsWithdrawalWhenNoBlockingReasonExists() {
		userRepository.save(User.create(7L, "bangpot"));

		GetMyWithdrawalCheckUseCase.Result result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(7L)
		);

		assertThat(result.canWithdraw()).isTrue();
		assertThat(result.blockingActiveCrews()).isEmpty();
		assertThat(result.blockingParticipatingMeetings()).isEmpty();
	}

	@Test
	void rejectsWithdrawalCheckWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(7L)))
			.isInstanceOf(AccessDeniedException.class);
	}
}
