package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.CheckCrewDeletionAvailabilityUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckCrewDeletionAvailabilityService implements CheckCrewDeletionAvailabilityUseCase {

	private static final String CHECK_DENIED_MESSAGE = "현재 크루장만 크루 삭제 가능 여부를 확인할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.leaderUserId(), CHECK_DENIED_MESSAGE);

		Crew crew = crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), query.leaderUserId())) {
			throw new AccessDeniedException(CHECK_DENIED_MESSAGE);
		}

		boolean hasOnlyLeader = !crewMemberRepository.existsActiveByCrewIdAndUserIdNot(
			crew.getId(),
			query.leaderUserId()
		);
		boolean hasNoUnfinishedMeetings = !meetingRepository.existsUnfinishedByCrewId(crew.getId());

		return Result.of(
			crew.getId(),
			hasOnlyLeader && hasNoUnfinishedMeetings,
			hasOnlyLeader,
			hasNoUnfinishedMeetings
		);
	}
}
