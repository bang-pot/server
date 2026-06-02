package com.banglog.meeting.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.banglog.meeting.application.port.MeetingLogFeedReadRepository;
import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingLogFeedReadRepository implements MeetingLogFeedReadRepository {

	private static final int EXCERPT_LIMIT = 120;

	private final MeetingLogFeedJpaRepository meetingLogFeedJpaRepository;

	@Override
	public CrewMeetingLogFeedView search(Long crewId, int page, int size) {
		List<CrewMeetingLogFeedView.Item> items = meetingLogFeedJpaRepository.findByCrewIdOrderByCreatedAtDesc(
				crewId,
				PageRequest.of(page, size + 1)
			).stream()
			.map(row -> CrewMeetingLogFeedView.Item.of(
				toLong(row.getLogId()),
				toLong(row.getMeetingId()),
				row.getAuthorNickname(),
				row.getMeetingTitle(),
				row.getThemeName(),
				row.getMeetingDate(),
				toInstant(row.getCreatedAt()),
				toExcerpt(row.getBody()),
				row.getCoverPhotoUrl(),
				toExtraPhotoCount(toLong(row.getTotalPhotoCount())),
				row.getResult()
			))
			.toList();

		boolean hasNext = items.size() > size;
		if (hasNext) {
			items = items.subList(0, size);
		}

		return CrewMeetingLogFeedView.of(items, CrewMeetingLogFeedView.Page.of(page, size, hasNext));
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

	private String toExcerpt(String body) {
		if (body == null || body.length() <= EXCERPT_LIMIT) {
			return body;
		}
		return body.substring(0, EXCERPT_LIMIT);
	}

	private long toExtraPhotoCount(Long totalPhotoCount) {
		if (totalPhotoCount == null || totalPhotoCount <= 1L) {
			return 0L;
		}
		return totalPhotoCount - 1L;
	}
}
