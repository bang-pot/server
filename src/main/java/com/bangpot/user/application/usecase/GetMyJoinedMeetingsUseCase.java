package com.bangpot.user.application.usecase;

import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;

public interface GetMyJoinedMeetingsUseCase {

	MyJoinedMeetingsView handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
		}
	}
}
