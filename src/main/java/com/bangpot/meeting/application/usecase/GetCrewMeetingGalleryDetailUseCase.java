package com.bangpot.meeting.application.usecase;

import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView;

public interface GetCrewMeetingGalleryDetailUseCase {

	CrewMeetingGalleryDetailView handle(Query query);

	record Query(
		Long crewId,
		Long meetingId,
		Long userId
	) {
		public static Query of(Long crewId, Long meetingId, Long userId) {
			return new Query(crewId, meetingId, userId);
		}
	}
}
