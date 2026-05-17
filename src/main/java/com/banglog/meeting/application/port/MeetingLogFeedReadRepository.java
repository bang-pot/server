package com.banglog.meeting.application.port;

import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;

public interface MeetingLogFeedReadRepository {

	CrewMeetingLogFeedView search(Long crewId, int page, int size);
}
