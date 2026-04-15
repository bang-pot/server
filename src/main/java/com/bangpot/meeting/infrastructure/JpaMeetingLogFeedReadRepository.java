package com.bangpot.meeting.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingLogFeedReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingLogFeedReadRepository implements MeetingLogFeedReadRepository {

	private final MeetingLogFeedJpaRepository meetingLogFeedJpaRepository;

	@Override
	public SearchResult search(Long crewId, int page, int size) {
		List<Item> items = meetingLogFeedJpaRepository.findByCrewIdOrderByCreatedAtDesc(
				crewId,
				PageRequest.of(page, size + 1)
			).stream()
			.map(row -> Item.of(
				toLong(row[0]),
				toLong(row[1]),
				toLong(row[2]),
				(String) row[3],
				(String) row[4],
				(String) row[5],
				(String) row[6],
				toInstant(row[7]),
				(String) row[8],
				(String) row[9],
				toLong(row[10])
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

	private Instant toInstant(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Instant instant) {
			return instant;
		}
		if (value instanceof OffsetDateTime offsetDateTime) {
			return offsetDateTime.toInstant();
		}
		if (value instanceof Timestamp timestamp) {
			return timestamp.toInstant();
		}
		throw new IllegalArgumentException("Unsupported createdAt type: " + value.getClass().getName());
	}
}
