package com.banglog.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.domain.view.MeetingsView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingsService implements GetMeetingsUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 모임 목록을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingAccessService meetingAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MeetingsView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		meetingAccessService.validateActiveCrewMember(query.crewId(), query.userId(), ACCESS_DENIED_MESSAGE);

		return meetingQueryRepository.findMeetingsViewByCrewId(query.crewId(), query.page(), query.size());
	}
}
