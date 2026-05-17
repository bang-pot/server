package com.banglog.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.GetMyJoinedMeetingsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyJoinedMeetingsService implements GetMyJoinedMeetingsUseCase {

	private final UserQueryRepository userQueryRepository;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	public MyJoinedMeetingsView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		return meetingQueryRepository.findMyJoinedMeetingsViewByUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
