package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.exception.MeetingNotFoundException;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingsView;
import com.banglog.auth.domain.AuthUser;

class MeetingCreationAndQueryUseCaseServicesTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void returnsMeetingsForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Sunday Escape", "Hidden Room", "Jamsil", "2026-04-22", "18:00", 5, null, null, null
		));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Saturday Escape", "Deep Blue", "Hongdae", "2026-04-21", "20:00", 6, null, null, null
		));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Friday Escape", "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null
		));

		MeetingsView result = getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), member.getId(), 0, 2));

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0).title()).isEqualTo("Friday Escape");
		assertThat(result.items().get(0).themeName()).isEqualTo("Time Attack");
		assertThat(result.items().get(0).status()).isEqualTo("RECRUITING");
		assertThat(result.items().get(1).title()).isEqualTo("Saturday Escape");
		assertThat(result.items().get(1).themeName()).isEqualTo("Deep Blue");
		assertThat(result.page().page()).isZero();
		assertThat(result.page().size()).isEqualTo(2);
		assertThat(result.page().hasNext()).isTrue();
	}

	@Test
	void returnsMeetingDetailForHostAsJoined() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			member.getId(),
			"Friday Escape",
			"Time Attack",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			120000,
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		));

		MeetingDetailView result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.hostUserId()).isEqualTo(member.getId());
		assertThat(result.title()).isEqualTo("Friday Escape");
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.contactLink()).isEqualTo("https://open.kakao.com/o/abc123");
		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
	}

	@Test
	void returnsNotJoinedWhenCurrentUserHasNotJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Friday Escape",
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));

		MeetingDetailView result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.myParticipationStatus()).isEqualTo("NOT_JOINED");
	}

	@Test
	void returnsJoinedWhenCurrentUserHasJoinedMeeting() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Friday Escape",
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		MeetingDetailView result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
	}

	@Test
	void rejectsMeetingAccessForTempUser() {
		AuthUser tempUser = tempUser(89L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), tempUser.getId()));

		assertThatThrownBy(() -> getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), tempUser.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingDetailForUnknownMeeting() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> getMeetingDetailUseCase.handle(GetMeetingDetailUseCase.Query.of(crew.getId(), 999L, member.getId())))
			.isInstanceOf(MeetingNotFoundException.class);
	}
}
