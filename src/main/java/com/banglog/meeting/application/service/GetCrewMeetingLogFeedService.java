package com.banglog.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.port.MeetingLogFeedReadRepository;
import com.banglog.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;
import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingLogFeedService implements GetCrewMeetingLogFeedUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "활성 크루 멤버만 모임 기록 피드를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingAccessService meetingAccessService;
	private final MeetingLogFeedReadRepository meetingLogFeedReadRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewMeetingLogFeedView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		meetingAccessService.validateActiveCrewMember(query.crewId(), query.userId(), ACCESS_DENIED_MESSAGE);

		return meetingLogFeedReadRepository.search(query.crewId(), query.page(), query.size());
	}
}
