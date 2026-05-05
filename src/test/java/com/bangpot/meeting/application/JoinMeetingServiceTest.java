package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyJoinedException;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

class JoinMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void joinsMeetingImmediatelyForJoinedCrewMember() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		JoinMeetingUseCase.Result result = joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), member.getId()))
			.isPresent()
			.get()
			.extracting(MeetingParticipant::getStatus)
			.isEqualTo(MeetingParticipationStatus.JOINED);
		assertThat(meetingRepository.findByIdAndCrewIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsJoinWhenAlreadyJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		assertThatThrownBy(() -> joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(MeetingParticipationAlreadyJoinedException.class);
	}

	@Test
	void rejectsJoinForHostWhoIsAlreadyJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingParticipationAlreadyJoinedException.class);
	}
}
