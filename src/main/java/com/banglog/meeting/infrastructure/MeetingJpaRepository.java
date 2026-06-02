package com.banglog.meeting.infrastructure;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banglog.crew.domain.CrewMemberStatus;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingResult;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.meeting.domain.view.CrewScheduleView;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingsAccessView;
import com.banglog.meeting.domain.view.MeetingsView;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.meeting.domain.view.UpcomingMeetingsView;

import jakarta.persistence.LockModeType;

interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

	interface UserMeetingCountRow {
		Long getUserId();
		long getMeetingCount();
	}

	List<Meeting> findAllByCrewIdOrderByMeetingDateAscMeetingTimeAscIdAsc(Long crewId);

	List<Meeting> findByStatusInOrderByMeetingDateAscMeetingTimeAscIdAsc(
		List<MeetingStatus> statuses,
		Pageable pageable
	);

	@Query("""
		select m
		from Meeting m
		where m.status = :status
		  and (
			m.meetingDate < :meetingDate
			or (m.meetingDate = :meetingDate and m.meetingTime <= :meetingTime)
		  )
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<Meeting> findDueMeetingsByStatus(
		@Param("status") MeetingStatus status,
		@Param("meetingDate") String meetingDate,
		@Param("meetingTime") String meetingTime,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.meeting.domain.view.MeetingsAccessView(
			c.id,
			member.role
		)
		from Crew c
		left join CrewMember member
		  on member.crewId = c.id
		 and member.userId = :userId
		 and member.status = :activeMemberStatus
		where c.id = :crewId
		  and c.status = :activeCrewStatus
		""")
	Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus
	);

	@Query("""
		select new com.banglog.meeting.domain.view.MeetingsView$Item(
			m.id,
			m.title,
			m.themeName,
			m.place,
			m.meetingDate,
			m.meetingTime,
			concat('', m.status),
			1 + (
				select count(mp.id)
				from MeetingParticipant mp
				where mp.meetingId = m.id
				  and mp.status in :joinedStatuses
				  and mp.userId <> m.hostUserId
			),
			m.capacity
		)
		from Meeting m
		where m.crewId = :crewId
		  and m.status in :includedStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	Slice<MeetingsView.Item> findMeetingItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("includedStatuses") List<MeetingStatus> includedStatuses,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.meeting.domain.view.MeetingDetailView(
			m.id,
			m.crewId,
			m.hostUserId,
			m.title,
			m.themeName,
			m.place,
			m.meetingDate,
			m.meetingTime,
			m.capacity,
			m.totalCost,
			m.contactLink,
			m.description,
			concat('', m.status),
			case
				when m.hostUserId = :userId then 'JOINED'
				when participant.status in :joinedStatuses then 'JOINED'
				else 'NOT_JOINED'
			end
		)
		from Meeting m
		left join MeetingParticipant participant
		  on participant.meetingId = m.id
		 and participant.userId = :userId
		where m.crewId = :crewId
		  and m.id = :meetingId
		""")
	Optional<MeetingDetailView> findMeetingDetailView(
		@Param("crewId") Long crewId,
		@Param("meetingId") Long meetingId,
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses
	);

	@Modifying(flushAutomatically = true)
	@Query("""
		update Meeting m
		set m.status = :canceledStatus,
		    m.updatedAt = :updatedAt
		where m.crewId = :crewId
		  and m.hostUserId = :hostUserId
		  and m.status in :unfinishedStatuses
		""")
	int cancelUnfinishedByCrewIdAndHostUserId(
		@Param("crewId") Long crewId,
		@Param("hostUserId") Long hostUserId,
		@Param("unfinishedStatuses") List<MeetingStatus> unfinishedStatuses,
		@Param("canceledStatus") MeetingStatus canceledStatus,
		@Param("updatedAt") Instant updatedAt
	);

	Optional<Meeting> findByIdAndCrewId(Long id, Long crewId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select meeting
		from Meeting meeting
		where meeting.id = :id
		  and meeting.crewId = :crewId
		""")
	Optional<Meeting> findByIdAndCrewIdForUpdate(
		@Param("id") Long id,
		@Param("crewId") Long crewId
	);

	@Query("""
		select new com.banglog.meeting.domain.view.MyCalendarView$Item(
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
		select new com.banglog.meeting.domain.view.MyCalendarView$Item(
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
		select new com.banglog.meeting.domain.view.MyCreatedMeetingsView$Item(
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
		select new com.banglog.meeting.domain.view.MyJoinedMeetingsView$Item(
			m.id,
			m.title,
			m.themeName,
			c.id,
			c.name,
			m.meetingDate,
			m.meetingTime,
			m.status,
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
		select new com.banglog.meeting.domain.view.UpcomingMeetingsView$Item(
			m.id,
			m.themeName,
			m.meetingDate,
			m.meetingTime
		)
		from Meeting m, Crew c
		where m.crewId = c.id
		  and c.status = :activeCrewStatus
		  and m.status in :upcomingStatuses
		  and (
			m.meetingDate > :currentDate
			or (m.meetingDate = :currentDate and m.meetingTime >= :currentTime)
		  )
		  and m.hostUserId = :userId
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<UpcomingMeetingsView.Item> findHostedUpcomingMeetingsViewByUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.meeting.domain.view.UpcomingMeetingsView$Item(
			m.id,
			m.themeName,
			m.meetingDate,
			m.meetingTime
		)
		from MeetingParticipant mp, Meeting m, Crew c
		where mp.meetingId = m.id
		  and m.crewId = c.id
		  and c.status = :activeCrewStatus
		  and mp.userId = :userId
		  and mp.status in :includedParticipationStatuses
		  and m.hostUserId <> :userId
		  and m.status in :upcomingStatuses
		  and (
			m.meetingDate > :currentDate
			or (m.meetingDate = :currentDate and m.meetingTime >= :currentTime)
		  )
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<UpcomingMeetingsView.Item> findJoinedUpcomingMeetingsViewByUserId(
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
		  and m.hostUserId = :userId
		""")
	long countHostedUpcomingMeetingsByUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime
	);

	@Query("""
		select count(mp.id)
		from MeetingParticipant mp, Meeting m, Crew c
		where mp.meetingId = m.id
		  and m.crewId = c.id
		  and c.status = :activeCrewStatus
		  and mp.userId = :userId
		  and mp.status in :includedParticipationStatuses
		  and m.hostUserId <> :userId
		  and m.status in :upcomingStatuses
		  and (
			m.meetingDate > :currentDate
			or (m.meetingDate = :currentDate and m.meetingTime >= :currentTime)
		  )
		""")
	long countJoinedUpcomingMeetingsByUserId(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("upcomingStatuses") List<MeetingStatus> upcomingStatuses,
		@Param("includedParticipationStatuses") List<MeetingParticipationStatus> includedParticipationStatuses,
		@Param("currentDate") String currentDate,
		@Param("currentTime") String currentTime
	);

	@Query("""
		select count(m)
		from Meeting m
		where m.hostUserId = :userId
		  and m.status = :completedStatus
		""")
	long countCompletedHostedActivityByUserId(
		@Param("userId") Long userId,
		@Param("completedStatus") MeetingStatus completedStatus
	);

	@Query("""
		select count(mp)
		from MeetingParticipant mp, Meeting m
		where mp.meetingId = m.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.status = :completedStatus
		  and m.hostUserId <> mp.userId
		""")
	long countCompletedJoinedActivityByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("completedStatus") MeetingStatus completedStatus
	);

	@Query("""
		select count(log)
		from MeetingLog log, Meeting m
		where log.meetingId = m.id
		  and log.authorUserId = :userId
		  and log.deletedAt is null
		  and m.hostUserId = :userId
		  and m.status = :completedStatus
		  and log.result = :successResult
		""")
	long countSuccessfulHostedActivityByUserId(
		@Param("userId") Long userId,
		@Param("completedStatus") MeetingStatus completedStatus,
		@Param("successResult") MeetingResult successResult
	);

	@Query("""
		select count(log)
		from MeetingLog log, MeetingParticipant mp, Meeting m
		where log.meetingId = m.id
		  and log.authorUserId = :userId
		  and log.deletedAt is null
		  and mp.meetingId = m.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.status = :completedStatus
		  and log.result = :successResult
		  and m.hostUserId <> mp.userId
		""")
	long countSuccessfulJoinedActivityByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("completedStatus") MeetingStatus completedStatus,
		@Param("successResult") MeetingResult successResult
	);

	@Query("""
		select new com.banglog.meeting.domain.view.CrewScheduleView$Item(
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
