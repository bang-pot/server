package com.banglog.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.user.application.usecase.GetMyCrewsUseCase;
import com.banglog.user.domain.User;

class GetMyCrewsServiceTest extends AbstractUserApplicationServiceTest {

	@Test
	void returnsMyActiveCrewsForCompletedUser() {
		userRepository.save(User.create(7L, "banglog"));
		userRepository.save(User.create(1L, "leader-pot"));

		Crew crew = crewRepository.save(Crew.create(
			"Alpha Crew",
			"crew",
			CrewVisibility.PUBLIC,
			"https://cdn.example.com/crew-alpha.jpg"
		));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), 1L));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), 7L));

		MyCrewsView result = getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20));

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).crewId()).isEqualTo(crew.getId());
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).visibility()).isEqualTo(CrewVisibility.PUBLIC);
		assertThat(result.items().get(0).leaderNickname()).isEqualTo("leader-pot");
		assertThat(result.items().get(0).coverImageUrl()).isEqualTo("https://cdn.example.com/crew-alpha.jpg");
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void defaultsCrewListToEmptyWhenNoActiveMembershipExists() {
		userRepository.save(User.create(7L, "banglog"));

		MyCrewsView result = getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20));

		assertThat(result.items()).isEmpty();
		assertThat(result.page().page()).isEqualTo(0);
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void returnsRequestedPageSlice() {
		userRepository.save(User.create(7L, "banglog"));
		userRepository.save(User.create(1L, "leader-a"));
		userRepository.save(User.create(2L, "leader-b"));

		Crew alphaCrew = crewRepository.save(Crew.create("Alpha Crew", "crew", CrewVisibility.PUBLIC, null));
		Crew betaCrew = crewRepository.save(Crew.create("Beta Crew", "crew", CrewVisibility.PRIVATE, null));

		crewMemberRepository.save(CrewMember.createLeader(alphaCrew.getId(), 1L));
		crewMemberRepository.save(CrewMember.createLeader(betaCrew.getId(), 2L));
		crewMemberRepository.save(CrewMember.createMember(alphaCrew.getId(), 7L));
		crewMemberRepository.save(CrewMember.createMember(betaCrew.getId(), 7L));

		MyCrewsView result = getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 1));

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.page().hasNext()).isTrue();
	}

	@Test
	void rejectsMyCrewsLookupWhenUserRowIsMissing() {
		assertThatThrownBy(() -> getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(7L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("프로필 완료가 필요합니다.");
	}
}
