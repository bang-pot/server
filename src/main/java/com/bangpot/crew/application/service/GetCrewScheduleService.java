package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.crew.domain.view.CrewMemberAccessView;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewScheduleService implements GetCrewScheduleUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 크루 일정을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewScheduleView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		CrewMemberAccessView access = crewQueryRepository.findCrewMemberAccessByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		)
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (access.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return meetingQueryRepository.findCrewScheduleViewByCrewId(query.crewId(), query.from(), query.to());
	}
}
