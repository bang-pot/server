package com.bangpot.user.infrastructure;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.user.application.port.MyMeetingLogReadRepository;

interface JpaMyMeetingLogReadRepository extends Repository<UserJpaEntity, Long>, MyMeetingLogReadRepository {

	@Override
	default SearchResult search(Long userId, int page, int size) {
		List<Row> rows = searchRows(userId, CrewStatus.ACTIVE, PageRequest.of(page, size + 1));
		boolean hasNext = rows.size() > size;
		List<Row> pageRows = hasNext ? rows.subList(0, size) : rows;
		return SearchResult.of(
			pageRows.stream()
				.map(row -> Item.of(
					row.getLogId(),
					row.getCrewId(),
					row.getCrewName(),
					row.getMeetingId(),
					row.getMeetingTitle(),
					row.getMeetingDate(),
					row.getCreatedAt(),
					row.getBody(),
					row.getCoverPhotoUrl(),
					row.getPhotoCount()
				))
				.toList(),
			PageInfo.of(page, size, hasNext)
		);
	}

	@Query("""
		select
			ml.id as logId,
			c.id as crewId,
			c.name as crewName,
			m.id as meetingId,
			m.title as meetingTitle,
			m.meetingDate as meetingDate,
			ml.createdAt as createdAt,
			ml.body as body,
			(
				select photo.photoUrl
				from MeetingLogPhoto photo
				where photo.id = (
					select min(firstPhoto.id)
					from MeetingLogPhoto firstPhoto
					where firstPhoto.logId = ml.id
				)
			) as coverPhotoUrl,
			(
				select count(photoCount)
				from MeetingLogPhoto photoCount
				where photoCount.logId = ml.id
			) as photoCount
		from MeetingLog ml, Meeting m, Crew c
		where ml.meetingId = m.id
		  and m.crewId = c.id
		  and ml.authorUserId = :userId
		  and ml.deletedAt is null
		  and c.status = :activeCrewStatus
		order by ml.createdAt desc, ml.id desc
		""")
	List<Row> searchRows(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		Pageable pageable
	);

	interface Row {
		Long getLogId();
		Long getCrewId();
		String getCrewName();
		Long getMeetingId();
		String getMeetingTitle();
		String getMeetingDate();
		Instant getCreatedAt();
		String getBody();
		String getCoverPhotoUrl();
		Long getPhotoCount();
	}
}
