package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.auth.domain.AuthUser;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.exception.MeetingRecruitmentReopenNotAllowedException;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingStatus;

class ReopenMeetingRecruitmentServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void reopensRecruitmentForMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();

		ReopenMeetingRecruitmentUseCase.Result result = reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(crewRepository.findByIdForShareCallCount).isEqualTo(1);
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}

	@Test
	void locksMeetingWhenReopeningRecruitment() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();
		meetingRepository.resetLockTracking();

		reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(meetingRepository.findByIdAndCrewIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsReopenForNonHostMemberWithoutChangingMeetingStatus() {
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
		meeting.closeRecruitment();

		assertThatThrownBy(() -> reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(AccessDeniedException.class);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
	}

	@Test
	void rejectsReopenWhenMeetingStartTimeAlreadyPassed() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-12", "18:00", 4, null, null, null, null
		));
		meeting.closeRecruitment();

		assertThatThrownBy(() -> reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingRecruitmentReopenNotAllowedException.class);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
	}

	@Test
	void rejectsReopenWhenMeetingCapacityIsFull() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 2, null, null, null, null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));
		meeting.closeRecruitment();

		assertThatThrownBy(() -> reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingRecruitmentReopenNotAllowedException.class);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
	}
}
