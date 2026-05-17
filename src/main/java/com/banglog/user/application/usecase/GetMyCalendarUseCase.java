package com.banglog.user.application.usecase;

import com.banglog.meeting.domain.view.MyCalendarView;

public interface GetMyCalendarUseCase {

	MyCalendarView handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}
}
