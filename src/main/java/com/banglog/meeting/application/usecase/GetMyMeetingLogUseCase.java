package com.banglog.meeting.application.usecase;

import com.banglog.meeting.domain.view.MyMeetingLogView;

public interface GetMyMeetingLogUseCase {

	MyMeetingLogView handle(Query query);

	record Query(
		Long meetingId,
		Long userId
	) {
		public static Query of(Long meetingId, Long userId) {
			return new Query(meetingId, userId);
		}
	}
}

