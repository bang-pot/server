package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;

interface MeetingLogJpaRepository extends JpaRepository<MeetingLog, Long> {

	Optional<MeetingLog> findByIdAndDeletedAtIsNull(Long id);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserIdAndDeletedAtIsNull(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserIdAndDeletedAtIsNotNull(Long meetingId, Long authorUserId);

	@Query("""
		select new com.bangpot.meeting.domain.view.MyMeetingLogsView.Item(
			ml.id,
			c.id,
			c.name,
			m.id,
			m.title,
			m.meetingDate,
			ml.createdAt,
			case
				when length(ml.body) > :excerptLimit then substring(ml.body, 1, :excerptLimit)
				else ml.body
			end,
			(
				select photo.photoUrl
				from MeetingLogPhoto photo
				where photo.id = (
					select min(firstPhoto.id)
					from MeetingLogPhoto firstPhoto
					where firstPhoto.logId = ml.id
				)
			),
			(
				select count(photoCount)
				from MeetingLogPhoto photoCount
				where photoCount.logId = ml.id
			)
		)
		from MeetingLog ml, Meeting m, Crew c
		where ml.meetingId = m.id
		  and m.crewId = c.id
		  and ml.authorUserId = :userId
		  and ml.deletedAt is null
		  and c.status = :activeCrewStatus
		order by ml.createdAt desc, ml.id desc
		""")
	Slice<MyMeetingLogsView.Item> findMyMeetingLogsViewByAuthorUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("excerptLimit") int excerptLimit,
		Pageable pageable
	);
}
