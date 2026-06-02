package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.auth.domain.AuthUser;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingStatus;

class CreateMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void createsMeetingWithDefaultStatusForJoinedMember() {
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

		Meeting saved = meetingRepository.findById(result.meetingId()).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
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
