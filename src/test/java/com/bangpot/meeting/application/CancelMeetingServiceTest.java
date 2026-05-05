package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

class CancelMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void cancelsMeetingForCrewLeader() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser leader = fullUser(78L, "leader-provider", "leader");
		authUserRepository.save(host);
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		CancelMeetingUseCase.Result result = cancelMeetingUseCase.handle(
			CancelMeetingUseCase.Command.of(crew.getId(), meeting.getId(), leader.getId())
		);

		assertThat(result.status()).isEqualTo("CANCELED");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.CANCELED);
	}

	@Test
	void locksMeetingWhenCanceling() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meetingRepository.resetLockTracking();

		cancelMeetingUseCase.handle(CancelMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId()));

		assertThat(meetingRepository.findByIdAndCrewIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsNonHostAndNonLeaderWithoutChangingMeetingStatus() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> cancelMeetingUseCase.handle(
			CancelMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(AccessDeniedException.class);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}
}
