package com.bangpot.meeting.application.port;

import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;

public interface MeetingQueryRepository {

	MyCalendarView findMyCalendarViewByUserId(Long userId);

	MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size);

	MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size);

	long countCreatedByHostUserId(Long userId);

	long countJoinedByUserId(Long userId);
}
