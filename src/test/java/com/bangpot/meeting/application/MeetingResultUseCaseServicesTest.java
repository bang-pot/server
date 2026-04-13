package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingResultAlreadyRecordedException;
import com.bangpot.meeting.application.exception.MeetingResultRecordNotAllowedException;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.RecordMeetingResultUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingResult;

class MeetingResultUseCaseServicesTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void recordsSuccessResultForCompletedMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();

		RecordMeetingResultUseCase.Result result = recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "SUCCESS")
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.result()).isEqualTo("SUCCESS");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getResult)
			.isEqualTo(MeetingResult.SUCCESS);
		assertThat(getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), host.getId())
		).result()).isEqualTo("SUCCESS");
		assertThat(getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), host.getId())))
			.singleElement()
			.extracting(GetMeetingsUseCase.View::result)
			.isEqualTo("SUCCESS");
	}

	@Test
	void rejectsMeetingResultRecordWhenMeetingIsNotCompleted() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "SUCCESS")
		)).isInstanceOf(MeetingResultRecordNotAllowedException.class);
	}

	@Test
	void rejectsMeetingResultRecordWhenResultAlreadyRecorded() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();
		recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "SUCCESS")
		);

		assertThatThrownBy(() -> recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "FAILURE")
		)).isInstanceOf(MeetingResultAlreadyRecordedException.class);
	}

	@Test
	void rejectsMeetingResultRecordForNonHostMember() {
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
		meeting.complete();

		assertThatThrownBy(() -> recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), member.getId(), "SUCCESS")
		)).isInstanceOf(AccessDeniedException.class);
	}
}
