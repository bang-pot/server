package com.bangpot.meeting.application.port;

import com.bangpot.meeting.domain.view.MyMeetingLogsView;

public interface MeetingLogQueryRepository {

	MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(Long userId, int page, int size);
}
