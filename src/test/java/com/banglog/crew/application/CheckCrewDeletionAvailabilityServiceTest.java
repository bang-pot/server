package com.banglog.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.service.CheckCrewDeletionAvailabilityService;
import com.banglog.crew.application.usecase.CheckCrewDeletionAvailabilityUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.user.application.service.CompletedUserAccessService;

class CheckCrewDeletionAvailabilityServiceTest {

	private CompletedUserAccessService completedUserAccessService;
	private CrewRepository crewRepository;
	private CrewMemberRepository crewMemberRepository;
	private MeetingRepository meetingRepository;
	private CheckCrewDeletionAvailabilityUseCase useCase;

	@BeforeEach
	void setUp() {
		completedUserAccessService = mock(CompletedUserAccessService.class);
		crewRepository = mock(CrewRepository.class);
		crewMemberRepository = mock(CrewMemberRepository.class);
		meetingRepository = mock(MeetingRepository.class);
		useCase = new CheckCrewDeletionAvailabilityService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
	}

	@Test
	void returnsUnavailableConditionResultsWhenUnfinishedMeetingExists() {
		Crew crew = crew(11L);
		when(crewRepository.findById(11L)).thenReturn(Optional.of(crew));
		when(crewMemberRepository.existsLeaderByCrewIdAndUserId(11L, 77L)).thenReturn(true);
		when(crewMemberRepository.existsActiveByCrewIdAndUserIdNot(11L, 77L)).thenReturn(false);
		when(meetingRepository.existsUnfinishedByCrewId(11L)).thenReturn(true);

		CheckCrewDeletionAvailabilityUseCase.Result result = useCase.handle(
			CheckCrewDeletionAvailabilityUseCase.Query.of(11L, 77L)
		);

		assertThat(result.crewId()).isEqualTo(11L);
		assertThat(result.canDelete()).isFalse();
		assertThat(result.hasOnlyLeader()).isTrue();
		assertThat(result.hasNoUnfinishedMeetings()).isFalse();
	}

	@Test
	void returnsAvailableWhenLeaderIsOnlyActiveMemberAndNoUnfinishedMeetingExists() {
		Crew crew = crew(11L);
		when(crewRepository.findById(11L)).thenReturn(Optional.of(crew));
		when(crewMemberRepository.existsLeaderByCrewIdAndUserId(11L, 77L)).thenReturn(true);
		when(crewMemberRepository.existsActiveByCrewIdAndUserIdNot(11L, 77L)).thenReturn(false);
		when(meetingRepository.existsUnfinishedByCrewId(11L)).thenReturn(false);

		CheckCrewDeletionAvailabilityUseCase.Result result = useCase.handle(
			CheckCrewDeletionAvailabilityUseCase.Query.of(11L, 77L)
		);

		assertThat(result.canDelete()).isTrue();
		assertThat(result.hasOnlyLeader()).isTrue();
		assertThat(result.hasNoUnfinishedMeetings()).isTrue();
	}

	@Test
	void rejectsCheckWhenCurrentUserIsNotLeader() {
		Crew crew = crew(11L);
		when(crewRepository.findById(11L)).thenReturn(Optional.of(crew));
		when(crewMemberRepository.existsLeaderByCrewIdAndUserId(11L, 77L)).thenReturn(false);

		assertThatThrownBy(() -> useCase.handle(CheckCrewDeletionAvailabilityUseCase.Query.of(11L, 77L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCheckWhenCrewDoesNotExist() {
		when(crewRepository.findById(11L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.handle(CheckCrewDeletionAvailabilityUseCase.Query.of(11L, 77L)))
			.isInstanceOf(CrewNotFoundException.class);
	}

	private Crew crew(Long id) {
		Crew crew = Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null);
		crew.assignId(id);
		return crew;
	}
}
