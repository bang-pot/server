package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView;

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
	List<GalleryCardRow> findGalleryCards(Long crewId, Pageable pageable);

	interface GalleryCardRow {

		Number getMeetingId();

		String getMeetingDate();

		String getMeetingTitle();

		String getCoverPhotoUrl();

		Number getExtraPhotoCount();
	}

	@Query("""
		select new com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView(
			m.id,
			m.meetingDate,
			m.title
		)
		from Meeting m
		where m.crewId = :crewId
		  and m.id = :meetingId
		  and m.status = com.bangpot.meeting.domain.MeetingStatus.COMPLETED
		  and exists (
				select 1
				from MeetingLog ml, MeetingLogPhoto mlp
				where mlp.logId = ml.id
				  and ml.meetingId = m.id
				  and ml.authorUserId = m.hostUserId
				  and ml.deletedAt is null
		  )
		""")
	Optional<CrewMeetingGalleryDetailTargetView> findGalleryDetailMeeting(Long crewId, Long meetingId);

	@Query("""
		select new com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView$PhotoSource(
			mlp.id,
			mlp.photoUrl
		)
		from MeetingLog ml, MeetingLogPhoto mlp
		where mlp.logId = ml.id
		  and ml.meetingId = :meetingId
		  and ml.deletedAt is null
		order by mlp.createdAt asc, mlp.id asc
		""")
	List<CrewMeetingGalleryDetailView.PhotoSource> findGalleryDetailPhotos(Long meetingId);
}
