package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingGalleryReadRepository implements MeetingGalleryReadRepository {

	private final MeetingGalleryJpaRepository meetingGalleryJpaRepository;

	@Override
	public Optional<Detail> findDetail(Long crewId, Long meetingId) {
		List<Object[]> detailRows = meetingGalleryJpaRepository.findGalleryDetailMeeting(crewId, meetingId);
		if (detailRows.isEmpty()) {
			return Optional.empty();
		}

		Object[] detailRow = detailRows.get(0);
		AtomicInteger order = new AtomicInteger(1);
		List<DetailPhoto> photos = meetingGalleryJpaRepository.findGalleryDetailPhotos(meetingId).stream()
			.map(row -> DetailPhoto.of(
				toLong(row[0]),
				(String) row[1],
				order.getAndIncrement()
			))
			.toList();

		return Optional.of(Detail.of(
			toLong(detailRow[0]),
			(String) detailRow[1],
			(String) detailRow[2],
			photos,
			photos.size()
		));
	}

	private Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		return ((Number)value).longValue();
	}
}
