package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.auth.domain.AuthUser;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.exception.MeetingResultAlreadyRecordedException;
import com.banglog.meeting.application.exception.MeetingResultRecordNotAllowedException;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.application.usecase.RecordMeetingResultUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingResult;
import com.banglog.meeting.domain.MeetingStatus;

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
			.extracting(view -> view.items().get(0).result())
			.isEqualTo("SUCCESS");
	}

	@Test
	void updatesMeetingTimestampWhenRecordingResult() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();
		clock.setInstant(NOW.plusSeconds(120));

		recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "SUCCESS")
		);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getUpdatedAt)
			.isEqualTo(NOW.plusSeconds(120));
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
	void doesNotCloseRecruitmentWhenRecordingResultBeforeMeetingCompletion() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-01", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> recordMeetingResultUseCase.handle(
			RecordMeetingResultUseCase.Command.of(crew.getId(), meeting.getId(), host.getId(), "SUCCESS")
		)).isInstanceOf(MeetingResultRecordNotAllowedException.class);

		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
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
