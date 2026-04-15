package com.bangpot.meeting.infrastructure;

import java.util.List;

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
		List<Item> items = meetingHistoryJpaRepository.findCrewCompletedMeetingHistory(
				crewId,
				userId,
				MeetingStatus.COMPLETED.name(),
				PageRequest.of(page, size + 1)
			).stream()
			.map(row -> Item.of(
				toLong(row[0]),
				(String) row[1],
				(String) row[2],
				(String) row[3],
				(String) row[4],
				String.valueOf(row[5]),
				toLong(row[6]),
				(String) row[7],
				toLong(row[8]),
				toLong(row[9]),
				(String) row[10]
			))
			.toList();

		boolean hasNext = items.size() > size;
		if (hasNext) {
			items = items.subList(0, size);
		}

		return SearchResult.of(items, PageInfo.of(page, size, hasNext));
	}

	private Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		return ((Number) value).longValue();
	}
}
