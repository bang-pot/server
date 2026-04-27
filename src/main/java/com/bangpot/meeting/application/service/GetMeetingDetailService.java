package com.bangpot.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingDetailService implements GetMeetingDetailUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 모임 상세를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingAccessService meetingAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MeetingDetailView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		meetingAccessService.validateActiveCrewMember(query.crewId(), query.userId(), ACCESS_DENIED_MESSAGE);

		return meetingQueryRepository.findMeetingDetailView(
			query.crewId(),
			query.meetingId(),
			query.userId()
		).orElseThrow(() -> new MeetingNotFoundException(query.meetingId()));
	}
}
