package com.bangpot.meeting.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingGalleryReadRepository implements MeetingGalleryReadRepository {

	private final MeetingGalleryJpaRepository meetingGalleryJpaRepository;

	@Override
	public SearchResult search(Long crewId, int page, int size) {
		List<Item> items = meetingGalleryJpaRepository.findGalleryCards(
				crewId,
				PageRequest.of(page, size + 1)
			).stream()
			.map(row -> Item.of(
				toLong(row[0]),
				(String) row[1],
				(String) row[2],
				(String) row[3],
				toLong(row[4])
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
		return ((Number)value).longValue();
	}
}
