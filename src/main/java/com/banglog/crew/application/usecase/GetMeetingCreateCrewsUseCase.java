package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.MeetingCreateCrewsView;

public interface GetMeetingCreateCrewsUseCase {

	MeetingCreateCrewsView handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

}
