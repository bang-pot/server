package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingHostCannotCancelParticipationException;
import com.bangpot.meeting.application.exception.MeetingParticipationNotJoinedException;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

class CancelMeetingParticipationServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void cancelsJoinedParticipationForJoinedCrewMember() {
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

		CancelMeetingParticipationUseCase.Result result = cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.myParticipationStatus()).isEqualTo("NOT_JOINED");
		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), member.getId())).get()
			.extracting(MeetingParticipant::getStatus)
			.isEqualTo(MeetingParticipationStatus.LEFT);
	}

	@Test
	void locksMeetingWhenCancelingParticipation() {
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
		meetingRepository.resetLockTracking();

		cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(meetingRepository.findByIdAndCrewIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsCancelWhenNotJoinedYet() {
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

		assertThatThrownBy(() -> cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(MeetingParticipationNotJoinedException.class);
	}

	@Test
	void rejectsCancelForMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingHostCannotCancelParticipationException.class);
	}
}
