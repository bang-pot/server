package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.JoinedMeetingReadRepository;

interface JpaJoinedMeetingReadRepository extends Repository<UserJpaEntity, Long>, JoinedMeetingReadRepository {

	@Override
	default SearchResult search(Long userId, int page, int size) {
		List<Row> rows = searchRows(userId, PageRequest.of(page, size + 1));
		boolean hasNext = rows.size() > size;
		List<Row> pageRows = hasNext ? rows.subList(0, size) : rows;
		return SearchResult.of(
			pageRows.stream()
				.map(row -> Item.of(
					row.getMeetingId(),
					row.getTitle(),
					row.getThemeName(),
					row.getCrewId(),
					row.getCrewName(),
					row.getDate(),
					row.getTime(),
					row.getStatus().name(),
					row.getStatus() == MeetingStatus.COMPLETED ? row.getResult().name() : null,
					row.isCanWriteReview()
				))
				.toList(),
			PageInfo.of(page, size, hasNext)
		);
	}

	@Query("""
		select
			m.id as meetingId,
			m.title as title,
			m.themeName as themeName,
			c.id as crewId,
			c.name as crewName,
			m.meetingDate as date,
			m.meetingTime as time,
			m.status as status,
			m.result as result,
			case
				when m.status = :completedStatus
				 and not exists (
					select 1
					from MeetingLog activeLog
					where activeLog.meetingId = m.id
					  and activeLog.authorUserId = :userId
					  and activeLog.deletedAt is null
				 )
				 and not exists (
					select 1
					from MeetingLog deletedLog
					where deletedLog.meetingId = m.id
					  and deletedLog.authorUserId = :userId
					  and deletedLog.deletedAt is not null
				 )
				then true
				else false
			end as canWriteReview
		from MeetingParticipant mp, Meeting m, Crew c
		where mp.meetingId = m.id
		  and m.crewId = c.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.hostUserId <> :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedMeetingStatuses
		order by m.meetingDate desc, m.meetingTime desc, m.id desc
		""")
	List<Row> searchRows(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses,
		@Param("completedStatus") MeetingStatus completedStatus,
		Pageable pageable
	);

	default List<Row> searchRows(Long userId, Pageable pageable) {
		return searchRows(
			userId,
			List.of(
				MeetingParticipationStatus.JOINED,
				MeetingParticipationStatus.PENDING,
				MeetingParticipationStatus.APPROVED
			),
			CrewStatus.ACTIVE,
			List.of(
				MeetingStatus.RECRUITING,
				MeetingStatus.RECRUITMENT_CLOSED,
				MeetingStatus.COMPLETED,
				MeetingStatus.CANCELED
			),
			MeetingStatus.COMPLETED,
			pageable
		);
	}

	interface Row {
		Long getMeetingId();
		String getTitle();
		String getThemeName();
		Long getCrewId();
		String getCrewName();
		String getDate();
		String getTime();
		MeetingStatus getStatus();
		MeetingResult getResult();
		boolean isCanWriteReview();
	}
}
