package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingEditNotAllowedException;
import com.bangpot.meeting.application.usecase.UpdateMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;

class UpdateMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void updatesRecruitingMeetingForHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Friday Escape",
			"Time Attack",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		));

		UpdateMeetingUseCase.Result result = updateMeetingUseCase.handle(UpdateMeetingUseCase.Command.of(
			crew.getId(),
			meeting.getId(),
			host.getId(),
			"Late Night Escape",
			"2026-04-22",
			"20:00",
			"Hongdae",
			"Deep Blue",
			2,
			90000,
			"https://open.kakao.com/o/new123",
			"Updated description"
		));

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.title()).isEqualTo("Late Night Escape");
		assertThat(result.themeName()).isEqualTo("Deep Blue");
		assertThat(result.place()).isEqualTo("Hongdae");
		assertThat(result.date()).isEqualTo("2026-04-22");
		assertThat(result.time()).isEqualTo("20:00");
		assertThat(result.capacity()).isEqualTo(2);
		assertThat(result.totalCost()).isEqualTo(90000);
		assertThat(result.contactLink()).isEqualTo("https://open.kakao.com/o/new123");
		assertThat(result.description()).isEqualTo("Updated description");

		Meeting updated = meetingRepository.findById(meeting.getId()).orElseThrow();
		assertThat(updated.getTitle()).isEqualTo("Late Night Escape");
		assertThat(updated.getThemeName()).isEqualTo("Deep Blue");
		assertThat(updated.getPlace()).isEqualTo("Hongdae");
		assertThat(updated.getMeetingDate()).isEqualTo("2026-04-22");
		assertThat(updated.getMeetingTime()).isEqualTo("20:00");
		assertThat(updated.getCapacity()).isEqualTo(2);
		assertThat(updated.getTotalCost()).isEqualTo(90000);
		assertThat(updated.getContactLink()).isEqualTo("https://open.kakao.com/o/new123");
		assertThat(updated.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void rejectsUpdateWhenCurrentUserIsNotHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Friday Escape", "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null
		));

		assertThatThrownBy(() -> updateMeetingUseCase.handle(UpdateMeetingUseCase.Command.of(
			crew.getId(),
			meeting.getId(),
			member.getId(),
			"Late Night Escape",
			"2026-04-22",
			"20:00",
			"Hongdae",
			"Deep Blue",
			2,
			null,
			null,
			null
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void allowsUpdateWhenMeetingIsRecruitmentClosed() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Friday Escape", "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null
		));
		meeting.closeRecruitment();

		UpdateMeetingUseCase.Result result = updateMeetingUseCase.handle(UpdateMeetingUseCase.Command.of(
			crew.getId(),
			meeting.getId(),
			host.getId(),
			"Late Night Escape",
			"2026-04-22",
			"20:00",
			"Hongdae",
			"Deep Blue",
			2,
			null,
			null,
			"Updated description"
		));

		assertThat(result.status()).isEqualTo("RECRUITMENT_CLOSED");
		assertThat(result.title()).isEqualTo("Late Night Escape");
		assertThat(result.themeName()).isEqualTo("Deep Blue");
	}

	@Test
	void rejectsUpdateWhenMeetingIsCompleted() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Friday Escape", "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();

		assertThatThrownBy(() -> updateMeetingUseCase.handle(UpdateMeetingUseCase.Command.of(
			crew.getId(),
			meeting.getId(),
			host.getId(),
			"Late Night Escape",
			"2026-04-22",
			"20:00",
			"Hongdae",
			"Deep Blue",
			2,
			null,
			null,
			"Updated description"
		))).isInstanceOf(MeetingEditNotAllowedException.class);
	}
}
