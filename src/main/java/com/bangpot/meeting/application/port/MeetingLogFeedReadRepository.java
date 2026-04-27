package com.bangpot.meeting.application.port;

import com.bangpot.meeting.domain.view.CrewMeetingLogFeedView;

public interface MeetingLogFeedReadRepository {

	CrewMeetingLogFeedView search(Long crewId, int page, int size);
}
