package com.banglog.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.exception.MeetingLogNotFoundException;
import com.banglog.meeting.application.port.MeetingLogQueryRepository;
import com.banglog.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.banglog.meeting.domain.view.MeetingLogDetailView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingLogDetailService implements GetMeetingLogDetailUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "방탈로그 상세 조회는 가입 완료 사용자만 가능합니다.";
	private static final String ACCESS_DENIED_MESSAGE = "활성 크루 멤버만 방탈로그 상세를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingAccessService meetingAccessService;
	private final MeetingLogQueryRepository meetingLogQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MeetingLogDetailView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		meetingAccessService.validateActiveCrewMember(query.crewId(), query.userId(), ACCESS_DENIED_MESSAGE);

		return meetingLogQueryRepository.findMeetingLogDetailView(query.crewId(), query.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(query.logId()));
	}
}
