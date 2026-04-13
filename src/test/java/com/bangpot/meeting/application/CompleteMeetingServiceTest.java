package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingInvalidStatusTransitionException;
import com.bangpot.meeting.application.usecase.CompleteMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

class CompleteMeetingServiceTest extends AbstractMeetingUseCaseServicesTest {

	@Test
	void completesMeetingForHostWhenRecruitmentIsClosed() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();

		CompleteMeetingUseCase.Result result = completeMeetingUseCase.handle(
			CompleteMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.status()).isEqualTo("COMPLETED");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.COMPLETED);
	}

	@Test
	void rejectsMeetingCompletionFromRecruitingState() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> completeMeetingUseCase.handle(
			CompleteMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingInvalidStatusTransitionException.class);
	}
}
