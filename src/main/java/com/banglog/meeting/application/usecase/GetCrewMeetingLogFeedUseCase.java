package com.banglog.meeting.application.usecase;

import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;

public interface GetCrewMeetingLogFeedUseCase {

	CrewMeetingLogFeedView handle(Query query);

	record Query(
		Long crewId,
		Long userId,
		int page,
		int size
	) {
		public static Query of(Long crewId, Long userId, int page, int size) {
			return new Query(crewId, userId, page, size);
		}
	}
}
