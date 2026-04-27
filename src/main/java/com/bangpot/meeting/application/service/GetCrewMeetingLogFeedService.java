package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogFeedReadRepository;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingLogFeedView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingLogFeedService implements GetCrewMeetingLogFeedUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "활성 크루 멤버만 모임 기록 피드를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingQueryRepository meetingQueryRepository;
	private final MeetingLogFeedReadRepository meetingLogFeedReadRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewMeetingLogFeedView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		MeetingsAccessView access = meetingQueryRepository.findMeetingsAccessViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return meetingLogFeedReadRepository.search(query.crewId(), query.page(), query.size());
	}
}
