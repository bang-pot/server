package com.bangpot.meeting.application.port;

import java.util.Optional;

import com.bangpot.meeting.domain.view.MeetingLogDetailView;
import com.bangpot.meeting.domain.view.MyMeetingLogView;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;

public interface MeetingLogQueryRepository {

	MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(Long userId, int page, int size);

	boolean existsMeetingById(Long meetingId);

	Optional<MyMeetingLogView> findMyMeetingLogView(Long meetingId, Long authorUserId);

	Optional<MeetingLogDetailView> findMeetingLogDetailView(Long crewId, Long logId);

	boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);
}
