package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.CreatedMeetingReadRepository;

interface JpaCreatedMeetingReadRepository extends Repository<UserJpaEntity, Long>, CreatedMeetingReadRepository {

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
					row.getStatus().name(),
					row.getDate(),
					row.getTime(),
					row.getCrewId(),
					row.getCrewName()
				))
				.toList(),
			PageInfo.of(page, size, hasNext)
		);
	}

	@Query("""
		select
			m.id as meetingId,
			m.title as title,
			m.status as status,
			m.meetingDate as date,
			m.meetingTime as time,
			c.id as crewId,
			c.name as crewName
		from Meeting m, Crew c
		where m.crewId = c.id
		  and m.hostUserId = :userId
		  and c.status = :activeStatus
		  and m.status in :includedStatuses
		order by m.meetingDate desc, m.meetingTime desc, m.id desc
		""")
	List<Row> searchRows(
		@Param("userId") Long userId,
		@Param("activeStatus") CrewStatus activeStatus,
		@Param("includedStatuses") List<MeetingStatus> includedStatuses,
		Pageable pageable
	);

	default List<Row> searchRows(Long userId, Pageable pageable) {
		return searchRows(
			userId,
			CrewStatus.ACTIVE,
			List.of(
				MeetingStatus.RECRUITING,
				MeetingStatus.RECRUITMENT_CLOSED,
				MeetingStatus.COMPLETED,
				MeetingStatus.CANCELED
			),
			pageable
		);
	}

	interface Row {
		Long getMeetingId();
		String getTitle();
		MeetingStatus getStatus();
		String getDate();
		String getTime();
		Long getCrewId();
		String getCrewName();
	}
}
