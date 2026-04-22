package com.bangpot.meeting.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaMeetingLogQueryRepository implements MeetingLogQueryRepository {

	private static final int EXCERPT_LIMIT = 120;

	private final MeetingLogJpaRepository meetingLogJpaRepository;

	@Override
	public MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyMeetingLogsView.Item> slice =
			meetingLogJpaRepository.findMyMeetingLogsViewByAuthorUserId(
			userId,
			CrewStatus.ACTIVE,
			EXCERPT_LIMIT,
			PageRequest.of(page, size)
			);
		return MyMeetingLogsView.of(slice.getContent(), MyMeetingLogsView.Page.of(page, size, slice.hasNext()));
	}
}
