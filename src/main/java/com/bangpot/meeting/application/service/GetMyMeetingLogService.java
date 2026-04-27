package com.bangpot.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.domain.view.MyMeetingLogView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyMeetingLogService implements GetMyMeetingLogUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "내 방탈로그 조회는 가입 완료 사용자만 가능합니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingLogQueryRepository meetingLogQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MyMeetingLogView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		if (!meetingLogQueryRepository.existsMeetingById(query.meetingId())) {
			throw new MeetingNotFoundException(query.meetingId());
		}
		return meetingLogQueryRepository.findMyMeetingLogView(
			query.meetingId(),
			query.userId()
		).orElseGet(() -> toMissingView(query));
	}

	private MyMeetingLogView toMissingView(Query query) {
		if (meetingLogQueryRepository.existsDeletedByMeetingIdAndAuthorUserId(query.meetingId(), query.userId())) {
			return MyMeetingLogView.deletedBlocked();
		}
		return MyMeetingLogView.notWritten();
	}
}
