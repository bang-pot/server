package com.bangpot.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;
import com.bangpot.meeting.domain.Meeting;

@DataJpaTest
@Import(JpaMeetingGalleryReadRepository.class)
class JpaMeetingGalleryReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingGalleryReadRepository repository;

	@Test
	void returnsCompletedMeetingsWithHostPhotosOnly() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'host', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'member', now(), now())")
			.executeUpdate();

		Meeting olderMeeting = completedMeeting(crew.getId(), 7L, "2026-04-11", "Older");
		Meeting newerMeeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "Newer");
		Meeting noHostPhotoMeeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "No Host Photo");
		Meeting noPhotoMeeting = completedMeeting(crew.getId(), 7L, "2026-04-13", "No Photo");
		Meeting recruitingMeeting = recruitingMeeting(crew.getId(), 7L, "2026-04-14", "Recruiting");

		Long olderHostLogId = insertLog(olderMeeting.getId(), 7L, "older host log", "2026-04-12T01:00:00Z");
		Long newerHostLogId = insertLog(newerMeeting.getId(), 7L, "newer host log", "2026-04-13T01:00:00Z");
		Long newerMemberLogId = insertLog(newerMeeting.getId(), 8L, "newer member log", "2026-04-13T02:00:00Z");
		Long noHostPhotoMemberLogId = insertLog(noHostPhotoMeeting.getId(), 8L, "member only photo", "2026-04-13T03:00:00Z");
		Long recruitingHostLogId = insertLog(recruitingMeeting.getId(), 7L, "recruiting host log", "2026-04-14T01:00:00Z");

		insertPhoto(olderHostLogId, "https://cdn.example.com/older-host-1.jpg", "2026-04-12T05:00:00Z");
		insertPhoto(newerHostLogId, "https://cdn.example.com/newer-host-1.jpg", "2026-04-13T06:00:00Z");
		insertPhoto(newerHostLogId, "https://cdn.example.com/newer-host-2.jpg", "2026-04-13T07:00:00Z");
		insertPhoto(newerMemberLogId, "https://cdn.example.com/newer-member-1.jpg", "2026-04-13T08:00:00Z");
		insertPhoto(noHostPhotoMemberLogId, "https://cdn.example.com/member-only-1.jpg", "2026-04-13T09:00:00Z");
		insertPhoto(recruitingHostLogId, "https://cdn.example.com/recruiting-host-1.jpg", "2026-04-14T02:00:00Z");

		entityManager.flush();
		entityManager.clear();

		MeetingGalleryReadRepository.SearchResult result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).extracting(MeetingGalleryReadRepository.Item::meetingId)
			.containsExactly(newerMeeting.getId(), olderMeeting.getId());
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/newer-host-1.jpg");
		assertThat(result.items().get(0).extraPhotoCount()).isEqualTo(2L);
		assertThat(result.items().get(1).coverPhotoUrl()).isEqualTo("https://cdn.example.com/older-host-1.jpg");
		assertThat(result.items().get(1).extraPhotoCount()).isEqualTo(0L);
		assertThat(result.pageInfo().hasNext()).isFalse();

		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(noHostPhotoMeeting.getId()));
		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(noPhotoMeeting.getId()));
		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(recruitingMeeting.getId()));
	}

	@Test
	void excludesSoftDeletedHostLogsFromRepresentativeSelection() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'host', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'member', now(), now())")
			.executeUpdate();

		Meeting meeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "Deleted Host Photo");
		Long deletedHostLogId = insertDeletedLog(meeting.getId(), 7L, "deleted host log", "2026-04-13T01:00:00Z");
		Long memberLogId = insertLog(meeting.getId(), 8L, "member log", "2026-04-13T02:00:00Z");

		insertPhoto(deletedHostLogId, "https://cdn.example.com/deleted-host-1.jpg", "2026-04-13T03:00:00Z");
		insertPhoto(memberLogId, "https://cdn.example.com/member-1.jpg", "2026-04-13T04:00:00Z");

		entityManager.flush();
		entityManager.clear();

		MeetingGalleryReadRepository.SearchResult result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).isEmpty();
	}

	@Test
	void returnsHasNextWhenMoreMeetingsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'host', now(), now())")
			.executeUpdate();

		Meeting firstMeeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "First");
		Meeting secondMeeting = completedMeeting(crew.getId(), 7L, "2026-04-11", "Second");

		Long firstLogId = insertLog(firstMeeting.getId(), 7L, "first", "2026-04-13T01:00:00Z");
		Long secondLogId = insertLog(secondMeeting.getId(), 7L, "second", "2026-04-12T01:00:00Z");
		insertPhoto(firstLogId, "https://cdn.example.com/first.jpg", "2026-04-13T02:00:00Z");
		insertPhoto(secondLogId, "https://cdn.example.com/second.jpg", "2026-04-12T02:00:00Z");

		entityManager.flush();
		entityManager.clear();

		MeetingGalleryReadRepository.SearchResult result = repository.search(crew.getId(), 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}

	@Test
	void returnsMeetingGalleryDetailWithAllActivePhotosInUploadOrder() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'host', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'member', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (9, 'deleted-member', now(), now())")
			.executeUpdate();

		Meeting meeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "Gallery Detail");
		Long hostLogId = insertLog(meeting.getId(), 7L, "host log", "2026-04-13T01:00:00Z");
		Long memberLogId = insertLog(meeting.getId(), 8L, "member log", "2026-04-13T02:00:00Z");
		Long deletedLogId = insertDeletedLog(meeting.getId(), 9L, "deleted member log", "2026-04-13T03:00:00Z");

		Long firstHostPhotoId = insertPhoto(hostLogId, "https://cdn.example.com/host-1.jpg", "2026-04-13T04:00:00Z");
		Long memberPhotoId = insertPhoto(memberLogId, "https://cdn.example.com/member-1.jpg", "2026-04-13T05:00:00Z");
		Long secondHostPhotoId = insertPhoto(hostLogId, "https://cdn.example.com/host-2.jpg", "2026-04-13T06:00:00Z");
		insertPhoto(deletedLogId, "https://cdn.example.com/deleted-1.jpg", "2026-04-13T07:00:00Z");

		entityManager.flush();
		entityManager.clear();

		java.util.Optional<MeetingGalleryReadRepository.Detail> result = repository.findDetail(crew.getId(), meeting.getId());

		assertThat(result).isPresent();
		assertThat(result.get().meetingId()).isEqualTo(meeting.getId());
		assertThat(result.get().meetingDate()).isEqualTo("2026-04-12");
		assertThat(result.get().meetingTitle()).isEqualTo("Gallery Detail");
		assertThat(result.get().totalPhotoCount()).isEqualTo(3);
		assertThat(result.get().photos()).extracting(MeetingGalleryReadRepository.DetailPhoto::photoId)
			.containsExactly(firstHostPhotoId, memberPhotoId, secondHostPhotoId);
		assertThat(result.get().photos()).extracting(MeetingGalleryReadRepository.DetailPhoto::url)
			.containsExactly(
				"https://cdn.example.com/host-1.jpg",
				"https://cdn.example.com/member-1.jpg",
				"https://cdn.example.com/host-2.jpg"
			);
		assertThat(result.get().photos()).extracting(MeetingGalleryReadRepository.DetailPhoto::order)
			.containsExactly(1, 2, 3);
	}

	@Test
	void returnsEmptyWhenMeetingIsNotGalleryTargetForDetail() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'host', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'member', now(), now())")
			.executeUpdate();

		Meeting noHostPhotoMeeting = completedMeeting(crew.getId(), 7L, "2026-04-12", "No Host Photo");
		Long memberLogId = insertLog(noHostPhotoMeeting.getId(), 8L, "member log", "2026-04-13T02:00:00Z");
		insertPhoto(memberLogId, "https://cdn.example.com/member-only.jpg", "2026-04-13T05:00:00Z");

		entityManager.flush();
		entityManager.clear();

		assertThat(repository.findDetail(crew.getId(), noHostPhotoMeeting.getId())).isEmpty();
	}

	private Meeting completedMeeting(Long crewId, Long hostUserId, String meetingDate, String title) {
		Meeting meeting = entityManager.persist(Meeting.create(
			crewId, hostUserId, title, "Deep Blue", "Hongdae", meetingDate, "20:00", 4, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();
		return entityManager.persistAndFlush(meeting);
	}

	private Meeting recruitingMeeting(Long crewId, Long hostUserId, String meetingDate, String title) {
		return entityManager.persistAndFlush(Meeting.create(
			crewId, hostUserId, title, "Deep Blue", "Hongdae", meetingDate, "20:00", 4, null, null, null
		));
	}

	private Long insertLog(Long meetingId, Long authorUserId, String body, String createdAt) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, ?, ?)
			""")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.setParameter(3, body)
			.setParameter(4, java.sql.Timestamp.from(java.time.Instant.parse(createdAt)))
			.setParameter(5, java.sql.Timestamp.from(java.time.Instant.parse(createdAt)))
			.executeUpdate();

		return ((Number) entityManager.getEntityManager()
			.createNativeQuery("select id from meeting_logs where meeting_id = ? and author_user_id = ?")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.getSingleResult()).longValue();
	}

	private Long insertDeletedLog(Long meetingId, Long authorUserId, String body, String createdAt) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (
				meeting_id,
				author_user_id,
				body,
				created_at,
				updated_at,
				deleted_at,
				deleted_by_user_id,
				deleted_by_role,
				delete_reason
			)
			values (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.setParameter(3, body)
			.setParameter(4, java.sql.Timestamp.from(java.time.Instant.parse(createdAt)))
			.setParameter(5, java.sql.Timestamp.from(java.time.Instant.parse(createdAt)))
			.setParameter(6, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-14T01:00:00Z")))
			.setParameter(7, authorUserId)
			.setParameter(8, "AUTHOR")
			.setParameter(9, null)
			.executeUpdate();

		return ((Number) entityManager.getEntityManager()
			.createNativeQuery("select id from meeting_logs where meeting_id = ? and author_user_id = ?")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.getSingleResult()).longValue();
	}

	private Long insertPhoto(Long logId, String url, String createdAt) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_log_photos (log_id, photo_url, created_at)
			values (?, ?, ?)
			""")
			.setParameter(1, logId)
			.setParameter(2, url)
			.setParameter(3, java.sql.Timestamp.from(java.time.Instant.parse(createdAt)))
			.executeUpdate();

		return ((Number) entityManager.getEntityManager()
			.createNativeQuery("select max(id) from meeting_log_photos where log_id = ?")
			.setParameter(1, logId)
			.getSingleResult()).longValue();
	}
}
