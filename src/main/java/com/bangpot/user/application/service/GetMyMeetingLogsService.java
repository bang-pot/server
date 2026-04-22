package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyMeetingLogsService implements GetMyMeetingLogsUseCase {

	private final UserQueryRepository userQueryRepository;
	private final MeetingLogQueryRepository meetingLogQueryRepository;

	@Override
	public MyMeetingLogsView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}
		return meetingLogQueryRepository.findMyMeetingLogsViewByAuthorUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
