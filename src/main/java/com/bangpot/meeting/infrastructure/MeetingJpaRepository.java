package com.bangpot.meeting.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;

interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

	interface UserMeetingCountRow {
		Long getUserId();
		long getMeetingCount();
	}

	List<Meeting> findAllByCrewIdOrderByMeetingDateAscMeetingTimeAscIdAsc(Long crewId);

	Optional<Meeting> findByIdAndCrewId(Long id, Long crewId);

	@Query("""
		select new com.bangpot.meeting.domain.view.MyCalendarView$Item(
			m.id,
			m.title,
			c.id,
			c.name,
			m.meetingDate,
			m.meetingTime,
			concat('', m.status),
			case when m.status = :canceledStatus then true else false end,
			'HOST'
		)
		from Meeting m, Crew c
		where m.crewId = c.id
		  and m.hostUserId = :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedMeetingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<MyCalendarView.Item> findMyHostedCalendarItemsByUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses,
		@Param("canceledStatus") MeetingStatus canceledStatus
	);

	@Query("""
		select new com.bangpot.meeting.domain.view.MyCalendarView$Item(
			m.id,
			m.title,
			c.id,
			c.name,
			m.meetingDate,
			m.meetingTime,
			concat('', m.status),
			case when m.status = :canceledStatus then true else false end,
			'PARTICIPANT'
		)
		from MeetingParticipant mp, Meeting m, Crew c
		where mp.meetingId = m.id
		  and m.crewId = c.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.hostUserId <> :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedMeetingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<MyCalendarView.Item> findMyParticipatingCalendarItemsByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses,
		@Param("canceledStatus") MeetingStatus canceledStatus
	);

	@Query("""
		select new com.bangpot.meeting.domain.view.MyCreatedMeetingsView$Item(
			m.id,
			m.title,
			m.status,
			m.meetingDate,
			m.meetingTime,
			c.id,
			c.name
		)
		from Meeting m, Crew c
		where m.crewId = c.id
		  and m.hostUserId = :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedStatuses
		order by m.meetingDate desc, m.meetingTime desc, m.id desc
		""")
	Slice<MyCreatedMeetingsView.Item> findMyCreatedMeetingsViewByHostUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedStatuses") List<MeetingStatus> includedStatuses,
		Pageable pageable
	);

	@Query("""
		select new com.bangpot.meeting.domain.view.MyJoinedMeetingsView$Item(
			m.id,
			m.title,
			m.themeName,
			c.id,
			c.name,
			m.meetingDate,
			m.meetingTime,
			m.status,
			case when m.status = :completedStatus then m.result else null end,
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
			end
		)
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
	Slice<MyJoinedMeetingsView.Item> findMyJoinedMeetingsViewByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses,
		@Param("completedStatus") MeetingStatus completedStatus,
		Pageable pageable
	);

	@Query("""
		select new com.bangpot.meeting.domain.view.UpcomingMeetingsView$Item(
			m.id,
			m.title,
			c.id,
			c.name,
			m.meetingDate,
			m.meetingTime,
			concat('', m.status)
		)
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
	List<UpcomingMeetingsView.Item> findUpcomingMeetingsViewByUserId(
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
	long countUpcomingMeetingsByUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("includedParticipationStatuses") List<MeetingParticipationStatus> includedParticipationStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime
	);

	@Query("""
		select new com.bangpot.meeting.domain.view.CrewScheduleView$Item(
			m.id,
			m.themeName,
			m.meetingDate,
			m.meetingTime,
			concat('', m.status),
			case when m.status = :recruitingStatus then 'OPEN' else 'CLOSED' end,
			m.place,
			(count(mp.id) + 1),
			case when m.status = :canceledStatus then true else false end
		)
		from Meeting m
		left join MeetingParticipant mp
		  on mp.meetingId = m.id
		 and mp.status in :joinedStatuses
		where m.crewId = :crewId
		  and m.status in :includedStatuses
		  and m.meetingDate between :from and :to
		group by
		  m.id,
		  m.themeName,
		  m.meetingDate,
		  m.meetingTime,
		  m.status,
		  m.place
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<CrewScheduleView.Item> findCrewScheduleItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("includedStatuses") List<MeetingStatus> includedStatuses,
		@Param("recruitingStatus") MeetingStatus recruitingStatus,
		@Param("canceledStatus") MeetingStatus canceledStatus,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("from") String from,
		@Param("to") String to
	);

	@Query("""
		select count(m)
		from Meeting m
		where m.hostUserId = :userId
		""")
	long countByHostUserId(@Param("userId") Long userId);

	@Query("""
		select count(mp)
		from MeetingParticipant mp, Meeting m
		where mp.meetingId = m.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.hostUserId <> :userId
		""")
	long countJoinedByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses
	);

	@Query("""
		select m.hostUserId as userId, count(m) as meetingCount
		from Meeting m
		where m.hostUserId in :userIds
		  and m.status = :completedStatus
		group by m.hostUserId
		""")
	List<UserMeetingCountRow> countCompletedHostedMeetingsByUserIds(
		@Param("userIds") Collection<Long> userIds,
		@Param("completedStatus") MeetingStatus completedStatus
	);

	@Query("""
		select mp.userId as userId, count(mp) as meetingCount
		from MeetingParticipant mp, Meeting m
		where mp.meetingId = m.id
		  and mp.userId in :userIds
		  and mp.status in :joinedStatuses
		  and m.status = :completedStatus
		  and m.hostUserId <> mp.userId
		group by mp.userId
		""")
	List<UserMeetingCountRow> countCompletedJoinedMeetingsByUserIds(
		@Param("userIds") Collection<Long> userIds,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("completedStatus") MeetingStatus completedStatus
	);

	boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses);

	boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses);
}
