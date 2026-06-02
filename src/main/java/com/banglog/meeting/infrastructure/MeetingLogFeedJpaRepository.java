package com.banglog.meeting.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.banglog.meeting.domain.MeetingLog;

interface MeetingLogFeedJpaRepository extends JpaRepository<MeetingLog, Long> {

	@Query(value = """
		select
			ml.id as logId,
			m.id as meetingId,
			u.nickname as authorNickname,
			m.title as meetingTitle,
			m.theme_name as themeName,
			m.meeting_date as meetingDate,
			ml.result as result,
			ml.created_at as createdAt,
			ml.body as body,
			(
				select mlp.photo_url
				from meeting_log_photos mlp
				where mlp.log_id = ml.id
				order by mlp.id asc
				limit 1
			) as coverPhotoUrl,
			(
				select count(*)
				from meeting_log_photos mlp
				where mlp.log_id = ml.id
			) as totalPhotoCount
		from meeting_logs ml
		join meetings m on m.id = ml.meeting_id
		join users u on u.id = ml.author_user_id
		where m.crew_id = :crewId
		  and ml.deleted_at is null
		order by ml.created_at desc, ml.id desc
		""", nativeQuery = true)
	List<FeedRow> findByCrewIdOrderByCreatedAtDesc(Long crewId, Pageable pageable);

	interface FeedRow {

		Number getLogId();

		Number getMeetingId();

		String getAuthorNickname();

		String getMeetingTitle();

		String getThemeName();

		String getMeetingDate();

		String getResult();

		Object getCreatedAt();

		String getBody();

		String getCoverPhotoUrl();

		Number getTotalPhotoCount();
	}
}
