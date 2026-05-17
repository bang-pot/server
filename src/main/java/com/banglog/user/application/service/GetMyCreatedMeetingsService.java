package com.banglog.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.GetMyCreatedMeetingsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyCreatedMeetingsService implements GetMyCreatedMeetingsUseCase {

	private final UserQueryRepository userQueryRepository;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	public MyCreatedMeetingsView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		return meetingQueryRepository.findMyCreatedMeetingsViewByHostUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
