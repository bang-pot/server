package com.bangpot.meeting.application.usecase;

import com.bangpot.meeting.domain.view.MeetingsView;

public interface GetMeetingsUseCase {

	MeetingsView handle(Query query);

	record Query(Long crewId, Long userId, int page, int size) {
		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId, 0, 20);
		}

		public static Query of(Long crewId, Long userId, int page, int size) {
			return new Query(crewId, userId, page, size);
		}
	}
}
