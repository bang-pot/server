package com.bangpot.home.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.home.application.port.HomeUpcomingMeetingReadRepository;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaHomeUpcomingMeetingReadRepositoryAdapter implements HomeUpcomingMeetingReadRepository {

	private static final List<MeetingStatus> UPCOMING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED
	);

	private static final List<MeetingParticipationStatus> INCLUDED_PARTICIPATION_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private final JpaHomeUpcomingMeetingReadRepository repository;

	@Override
	public Result findUpcomingMeetings(Long userId, int limit, String currentDate, String currentTime) {
		List<JpaHomeUpcomingMeetingReadRepository.Row> rows = repository.findRows(
			userId,
			CrewStatus.ACTIVE,
			UPCOMING_STATUSES,
			INCLUDED_PARTICIPATION_STATUSES,
			currentDate,
			currentTime,
			PageRequest.of(0, limit)
		);

		return Result.of(
			rows.stream()
				.map(row -> Item.of(
					row.getMeetingId(),
					row.getTitle(),
					row.getCrewId(),
					row.getCrewName(),
					row.getDate(),
					row.getTime(),
					row.getStatus().name()
				))
				.toList(),
			repository.countRows(
				userId,
				CrewStatus.ACTIVE,
				UPCOMING_STATUSES,
				INCLUDED_PARTICIPATION_STATUSES,
				currentDate,
				currentTime
			)
		);
	}
}
