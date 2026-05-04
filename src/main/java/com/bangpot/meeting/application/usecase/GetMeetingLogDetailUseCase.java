package com.bangpot.meeting.application.usecase;

import com.bangpot.meeting.domain.view.MeetingLogDetailView;

public interface GetMeetingLogDetailUseCase {

	MeetingLogDetailView handle(Query query);

	record Query(
		Long crewId,
		Long logId,
		Long userId
	) {
		public static Query of(Long crewId, Long logId, Long userId) {
			return new Query(crewId, logId, userId);
		}
	}
}

