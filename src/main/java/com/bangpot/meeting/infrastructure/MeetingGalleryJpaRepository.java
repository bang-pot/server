package com.bangpot.meeting.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bangpot.meeting.domain.MeetingLog;

interface MeetingGalleryJpaRepository extends JpaRepository<MeetingLog, Long> {

	@Query(value = """
		select
			m.id as meetingId,
			m.meeting_date as meetingDate,
			m.title as meetingTitle,
			(
				select mlp.photo_url
				from meeting_logs ml
				join meeting_log_photos mlp on mlp.log_id = ml.id
				where ml.meeting_id = m.id
				  and ml.author_user_id = m.host_user_id
				  and ml.deleted_at is null
				order by mlp.created_at asc, mlp.id asc
				limit 1
			) as coverPhotoUrl,
			greatest((
				select count(*)
				from meeting_logs ml
				join meeting_log_photos mlp on mlp.log_id = ml.id
				where ml.meeting_id = m.id
				  and ml.deleted_at is null
			) - 1, 0) as extraPhotoCount
		from meetings m
		where m.crew_id = :crewId
		  and m.status = 'COMPLETED'
		  and exists (
				select 1
				from meeting_logs ml
				join meeting_log_photos mlp on mlp.log_id = ml.id
				where ml.meeting_id = m.id
				  and ml.author_user_id = m.host_user_id
				  and ml.deleted_at is null
		  )
		order by m.meeting_date desc,
			(
				select mlp.created_at
				from meeting_logs ml
				join meeting_log_photos mlp on mlp.log_id = ml.id
				where ml.meeting_id = m.id
				  and ml.author_user_id = m.host_user_id
				  and ml.deleted_at is null
				order by mlp.created_at asc, mlp.id asc
				limit 1
			) desc,
			m.id desc
		""", nativeQuery = true)
	List<Object[]> findGalleryCards(Long crewId, Pageable pageable);
}
