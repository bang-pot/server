package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MeetingDetailView;

class MeetingAutomaticTransitionUseCaseServicesTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void closesRecruitmentAutomaticallyWhenCapacityIsReachedAfterJoin() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Capacity Theme", "Gangnam", "2026-04-13", "20:00", 2, null, null, null, null
		));

		joinMeetingUseCase.handle(JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId()));

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
	}

	@Test
	void doesNotCloseRecruitmentAutomaticallyOnDetailRead() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Time Theme", "Gangnam", "2026-04-12", "17:00", 4, null, null, null, null
		));

		MeetingDetailView result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}

	@Test
	void doesNotCompleteMeetingAutomaticallyOnListRead() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Complete Theme", "Gangnam", "2026-04-12", "10:00", 4, null, null, null, null
		));

		var result = getMeetingsUseCase.handle(
			com.bangpot.meeting.application.usecase.GetMeetingsUseCase.Query.of(crew.getId(), host.getId())
		);

		assertThat(result.items()).singleElement()
			.extracting(com.bangpot.meeting.domain.view.MeetingsView.Item::status)
			.isEqualTo("RECRUITING");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}
}
