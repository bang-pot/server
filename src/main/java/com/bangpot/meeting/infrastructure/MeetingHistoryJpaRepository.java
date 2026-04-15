package com.bangpot.meeting.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

interface MeetingHistoryJpaRepository extends Repository<Meeting, Long> {

	@Query("""
		select m.id as meetingId,
		       m.title as meetingTitle,
		       m.themeName as themeName,
		       m.place as place,
		       m.meetingDate as meetingDate,
		       m.result as result,
		       ml.id as logId
		from Meeting m
		left join MeetingLog ml
		  on ml.meetingId = m.id
		 and ml.authorUserId = :userId
		where m.crewId = :crewId
		  and m.status = :completedStatus
		order by m.meetingDate desc, m.meetingTime desc, m.id desc
		""")
	Slice<MeetingHistoryProjection> findCrewCompletedMeetingHistory(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("completedStatus") MeetingStatus completedStatus,
		Pageable pageable
	);

	interface MeetingHistoryProjection {
		Long getMeetingId();

		String getMeetingTitle();

		String getThemeName();

		String getPlace();

		String getMeetingDate();

		com.bangpot.meeting.domain.MeetingResult getResult();

		Long getLogId();
	}
}
