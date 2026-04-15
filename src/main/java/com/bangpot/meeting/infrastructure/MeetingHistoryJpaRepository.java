package com.bangpot.meeting.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bangpot.meeting.domain.Meeting;

interface MeetingHistoryJpaRepository extends JpaRepository<Meeting, Long> {

	@Query(value = """
		select
			m.id as meetingId,
			m.title as meetingTitle,
			m.theme_name as themeName,
			m.place as place,
			m.meeting_date as meetingDate,
			m.result as result,
			current_ml.id as logId,
			case
				when latest_ml.body is null then null
				when char_length(latest_ml.body) <= 120 then latest_ml.body
				else substring(latest_ml.body, 1, 120)
			end as reviewSummary,
			(
				select count(*)
				from meeting_logs all_ml
				where all_ml.meeting_id = m.id
			) as logCount,
			1 + (
				select count(*)
				from meeting_participation_requests p
				where p.meeting_id = m.id
				  and p.status in ('JOINED', 'PENDING', 'APPROVED')
			) as participantCount,
			(
				select mlp.photo_url
				from meeting_logs log_for_photo
				join meeting_log_photos mlp on mlp.log_id = log_for_photo.id
				where log_for_photo.meeting_id = m.id
				order by log_for_photo.created_at desc, log_for_photo.id desc, mlp.id asc
				limit 1
			) as coverPhotoUrl
		from meetings m
		left join meeting_logs current_ml
			on current_ml.meeting_id = m.id
		   and current_ml.author_user_id = :userId
		left join meeting_logs latest_ml
			on latest_ml.id = (
				select latest.id
				from meeting_logs latest
				where latest.meeting_id = m.id
				order by latest.created_at desc, latest.id desc
				limit 1
			)
		where m.crew_id = :crewId
		  and m.status = :completedStatus
		order by m.meeting_date desc, m.meeting_time desc, m.id desc
		""", nativeQuery = true)
	List<Object[]> findCrewCompletedMeetingHistory(
		Long crewId,
		Long userId,
		String completedStatus,
		Pageable pageable
	);
}
