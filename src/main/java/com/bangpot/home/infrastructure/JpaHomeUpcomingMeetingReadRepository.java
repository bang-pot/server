package com.bangpot.home.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

interface JpaHomeUpcomingMeetingReadRepository extends Repository<Meeting, Long> {

	@Query("""
		select
			m.id as meetingId,
			m.title as title,
			c.id as crewId,
			c.name as crewName,
			m.meetingDate as date,
			m.meetingTime as time,
			m.status as status
		from Meeting m, Crew c
		where m.crewId = c.id
		  and c.status = :activeCrewStatus
		  and m.status in :upcomingStatuses
		  and (
			m.meetingDate > :currentDate
			or (m.meetingDate = :currentDate and m.meetingTime >= :currentTime)
		  )
		  and (
			m.hostUserId = :userId
			or exists (
				select 1
				from MeetingParticipant mp
				where mp.meetingId = m.id
				  and mp.userId = :userId
				  and mp.status in :includedParticipationStatuses
			)
		  )
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<Row> findRows(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("includedParticipationStatuses") List<MeetingParticipationStatus> includedParticipationStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime,
		Pageable pageable
	);

	@Query("""
		select count(m.id)
		from Meeting m, Crew c
		where m.crewId = c.id
		  and c.status = :activeCrewStatus
		  and m.status in :upcomingStatuses
		  and (
			m.meetingDate > :currentDate
			or (m.meetingDate = :currentDate and m.meetingTime >= :currentTime)
		  )
		  and (
			m.hostUserId = :userId
			or exists (
				select 1
				from MeetingParticipant mp
				where mp.meetingId = m.id
				  and mp.userId = :userId
				  and mp.status in :includedParticipationStatuses
			)
		  )
		""")
	long countRows(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("includedParticipationStatuses") List<MeetingParticipationStatus> includedParticipationStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime
	);

	interface Row {
		Long getMeetingId();
		String getTitle();
		Long getCrewId();
		String getCrewName();
		String getDate();
		String getTime();
		MeetingStatus getStatus();
	}
}
