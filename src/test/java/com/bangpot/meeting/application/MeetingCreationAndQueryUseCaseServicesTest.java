package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.auth.domain.AuthUser;

class MeetingCreationAndQueryUseCaseServicesTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void returnsMeetingsForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Deep Blue", "Hongdae", "2026-04-21", "20:00", 6, null, null, null, null
		));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		List<GetMeetingsUseCase.View> result = getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), member.getId()));

		assertThat(result).hasSize(2);
		assertThat(result.get(0).themeName()).isEqualTo("Time Attack");
		assertThat(result.get(0).status()).isEqualTo("RECRUITING");
		assertThat(result.get(1).themeName()).isEqualTo("Deep Blue");
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

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.hostUserId()).isEqualTo(member.getId());
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");
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

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
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

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
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
