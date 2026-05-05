package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;

class CreateMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void createsMeetingWithDefaultStatusAndResultForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		CreateMeetingUseCase.Result result = createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(),
			member.getId(),
			"Friday Escape",
			"2026-04-20",
			"19:30",
			"Gangnam",
			"Time Attack",
			4,
			120000,
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		));

		assertThat(result.meetingId()).isNotNull();
		assertThat(result.title()).isEqualTo("Friday Escape");
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");

		Meeting saved = meetingRepository.findById(result.meetingId()).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
		assertThat(saved.getResult()).isEqualTo(MeetingResult.NOT_RECORDED);
		assertThat(saved.getHostUserId()).isEqualTo(member.getId());
		assertThat(saved.getTitle()).isEqualTo("Friday Escape");
		assertThat(saved.getContactLink()).isEqualTo("https://open.kakao.com/o/abc123");
		assertThat(crewRepository.findByIdForShareCallCount).isEqualTo(1);
	}

	@Test
	void rejectsMeetingCreateForNonMember() {
		AuthUser outsider = fullUser(88L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(), outsider.getId(), "Friday Escape", "2026-04-20", "19:30", "Gangnam", "Time Attack", 4, null, null, null
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void validatesCompletedUserBeforeTakingCrewShareLock() {
		AuthUser tempUser = tempUser(99L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(), tempUser.getId(), "Friday Escape", "2026-04-20", "19:30", "Gangnam", "Time Attack", 4, null, null, null
		))).isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("가입한 크루원만 모임을 생성할 수 있습니다.");

		assertThat(crewRepository.findByIdForShareCallCount).isZero();
	}
}
