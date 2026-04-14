package com.bangpot.archive.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.archive.application.port.ArchiveMeetingReadRepository;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaArchiveMeetingReadRepository implements ArchiveMeetingReadRepository {

	private final ArchiveMeetingJpaRepository archiveMeetingJpaRepository;

	@Override
	public SearchResult search(Long userId, int page, int size) {
		var slice = archiveMeetingJpaRepository.findCompletedArchiveMeetings(
			userId,
			MeetingStatus.COMPLETED,
			List.of(
				MeetingParticipationStatus.JOINED,
				MeetingParticipationStatus.LEFT,
				MeetingParticipationStatus.PENDING,
				MeetingParticipationStatus.APPROVED
			),
			PageRequest.of(page, size)
		);

		return SearchResult.of(
			slice.getContent().stream()
				.map(item -> Item.of(
					item.getMeetingId(),
					item.getCrewId(),
					item.getCrewName(),
					item.getThemeName(),
					item.getPlace(),
					item.getMeetingDate(),
					item.getResult().name()
				))
				.toList(),
			PageInfo.of(page, size, slice.hasNext())
		);
	}
}
