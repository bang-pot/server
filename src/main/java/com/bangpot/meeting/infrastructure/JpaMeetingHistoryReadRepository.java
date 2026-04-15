package com.bangpot.meeting.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingHistoryReadRepository;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingHistoryReadRepository implements MeetingHistoryReadRepository {

	private final MeetingHistoryJpaRepository meetingHistoryJpaRepository;

	@Override
	public SearchResult search(Long crewId, Long userId, int page, int size) {
		var slice = meetingHistoryJpaRepository.findCrewCompletedMeetingHistory(
			crewId,
			userId,
			MeetingStatus.COMPLETED,
			PageRequest.of(page, size)
		);

		return SearchResult.of(
			slice.getContent().stream()
				.map(item -> Item.of(
					item.getMeetingId(),
					item.getMeetingTitle(),
					item.getThemeName(),
					item.getPlace(),
					item.getMeetingDate(),
					item.getResult().name(),
					item.getLogId()
				))
				.toList(),
			PageInfo.of(page, size, slice.hasNext())
		);
	}
}
