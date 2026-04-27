package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingsService implements GetMeetingsUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 모임 목록을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MeetingsView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		MeetingsAccessView access = meetingQueryRepository.findMeetingsAccessViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return meetingQueryRepository.findMeetingsViewByCrewId(query.crewId(), query.page(), query.size());
	}
}
