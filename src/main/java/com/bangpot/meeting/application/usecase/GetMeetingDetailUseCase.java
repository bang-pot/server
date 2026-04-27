package com.bangpot.meeting.application.usecase;

import com.bangpot.meeting.domain.view.MeetingDetailView;

public interface GetMeetingDetailUseCase {

	MeetingDetailView handle(Query query);

	record Query(Long crewId, Long meetingId, Long userId) {
		public static Query of(Long crewId, Long meetingId, Long userId) {
			return new Query(crewId, meetingId, userId);
		}
	}
}
