package com.banglog.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banglog.crew.domain.CrewStatus;
import com.banglog.meeting.domain.MeetingLog;
import com.banglog.meeting.domain.view.MeetingLogDetailView;
import com.banglog.meeting.domain.view.MyMeetingLogView;
import com.banglog.meeting.domain.view.MyMeetingLogsView;

import jakarta.persistence.LockModeType;

interface MeetingLogJpaRepository extends JpaRepository<MeetingLog, Long> {

	Optional<MeetingLog> findByIdAndDeletedAtIsNull(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select log
		from MeetingLog log
		where log.id = :id
		  and log.deletedAt is null
		""")
	Optional<MeetingLog> findActiveByIdForUpdate(@Param("id") Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select log
		from MeetingLog log, Meeting meeting
		where log.meetingId = meeting.id
		  and log.id = :logId
		  and meeting.crewId = :crewId
		  and log.deletedAt is null
		""")
	Optional<MeetingLog> findActiveLogInCrewForUpdate(
		@Param("crewId") Long crewId,
		@Param("logId") Long logId
	);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserIdAndDeletedAtIsNull(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserIdAndDeletedAtIsNotNull(Long meetingId, Long authorUserId);

	@Query("""
		select count(m) > 0
		from Meeting m
		where m.id = :meetingId
		""")
	boolean existsMeetingById(@Param("meetingId") Long meetingId);

	@Query("""
		select new com.banglog.meeting.domain.view.MyMeetingLogView$Source(
			ml.id,
			m.id,
			m.title,
			m.themeName,
			m.place,
			m.meetingDate,
			u.nickname,
			ml.createdAt,
			ml.updatedAt,
			ml.body
		)
		from MeetingLog ml, Meeting m, UserJpaEntity u
		where ml.meetingId = m.id
		  and ml.authorUserId = u.id
		  and ml.meetingId = :meetingId
		  and ml.authorUserId = :authorUserId
		  and ml.deletedAt is null
		  and u.withdrawnAt is null
		""")
	Optional<MyMeetingLogView.Source> findMyMeetingLogSource(
		@Param("meetingId") Long meetingId,
		@Param("authorUserId") Long authorUserId
	);

	@Query("""
		select new com.banglog.meeting.domain.view.MeetingLogDetailView$Source(
			ml.id,
			m.id,
			m.title,
			m.themeName,
			m.place,
			m.meetingDate,
			u.nickname,
			ml.createdAt,
			ml.updatedAt,
			ml.body
		)
		from MeetingLog ml, Meeting m, UserJpaEntity u
		where ml.meetingId = m.id
		  and ml.authorUserId = u.id
		  and ml.id = :logId
		  and m.crewId = :crewId
		  and ml.deletedAt is null
		  and u.withdrawnAt is null
		""")
	Optional<MeetingLogDetailView.Source> findMeetingLogDetailSource(
		@Param("crewId") Long crewId,
		@Param("logId") Long logId
	);

	@Query("""
		select photo.photoUrl
		from MeetingLogPhoto photo
		where photo.logId = :logId
		order by photo.id asc
		""")
	List<String> findPhotoUrlsByLogId(@Param("logId") Long logId);

	@Query("""
		select new com.banglog.meeting.domain.view.MyMeetingLogsView$Item(
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
