package com.bangpot.meeting.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

interface ArchivedMeetingJpaRepository extends Repository<Meeting, Long> {

	@Query("""
		select m.id as meetingId,
		       c.id as crewId,
		       c.name as crewName,
		       m.themeName as themeName,
		       m.place as place,
		       m.meetingDate as meetingDate,
		       m.result as result
		from Meeting m, com.bangpot.crew.domain.Crew c
		where c.id = m.crewId
		  and m.status = :completedStatus
		  and (
		        m.hostUserId = :userId
		        or exists (
		            select 1
		            from MeetingParticipant p
		            where p.meetingId = m.id
		              and p.userId = :userId
		              and p.status in :historyStatuses
		        )
		      )
		order by m.meetingDate desc, m.meetingTime desc, m.id desc
		""")
	Slice<ArchiveMeetingProjection> findCompletedArchiveMeetings(
		@Param("userId") Long userId,
		@Param("completedStatus") MeetingStatus completedStatus,
		@Param("historyStatuses") List<MeetingParticipationStatus> historyStatuses,
		Pageable pageable
	);

	interface ArchiveMeetingProjection {
		Long getMeetingId();

		Long getCrewId();

		String getCrewName();

		String getThemeName();

		String getPlace();

		String getMeetingDate();

		com.bangpot.meeting.domain.MeetingResult getResult();
	}
}

