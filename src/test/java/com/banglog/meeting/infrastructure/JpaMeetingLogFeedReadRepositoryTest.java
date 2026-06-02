package com.banglog.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingLogFeedReadRepository;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;

@DataJpaTest
@Import(JpaMeetingLogFeedReadRepository.class)
class JpaMeetingLogFeedReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingLogFeedReadRepository repository;

	@Test
	void returnsLatestLogsWithCoverPhotoAndTotalPhotoCount() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		insertUser(7L, "writer");
		insertUser(8L, "writer2");

		Meeting meeting1 = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		Meeting meeting2 = entityManager.persist(Meeting.create(
			crew.getId(), 8L, "Friday 2", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));

		insertLog(meeting1.getId(), 7L, "First log body for feed card.", "SUCCESS", "2026-04-14T01:00:00Z");
		insertLog(meeting2.getId(), 8L, "Second log body.", "FAILURE", "2026-04-15T01:00:00Z");

		Long log1Id = findLogId(meeting1.getId());
		Long log2Id = findLogId(meeting2.getId());

		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_log_photos (log_id, photo_url, created_at)
			values (?, 'https://cdn.example.com/a.jpg', now()),
			       (?, 'https://cdn.example.com/b.jpg', now()),
			       (?, 'https://cdn.example.com/c.jpg', now())
			""")
			.setParameter(1, log1Id)
			.setParameter(2, log1Id)
			.setParameter(3, log2Id)
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		CrewMeetingLogFeedView result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).extracting(CrewMeetingLogFeedView.Item::meetingId)
			.containsExactly(meeting2.getId(), meeting1.getId());
		assertThat(result.items()).extracting(CrewMeetingLogFeedView.Item::themeName)
			.containsExactly("Time Attack", "Deep Blue");
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/c.jpg");
		assertThat(result.items().get(0).extraPhotoCount()).isZero();
		assertThat(result.items().get(0).result()).isEqualTo("FAILURE");
		assertThat(result.items().get(1).coverPhotoUrl()).isEqualTo("https://cdn.example.com/a.jpg");
		assertThat(result.items().get(1).extraPhotoCount()).isEqualTo(1);
		assertThat(result.items().get(1).result()).isEqualTo("SUCCESS");
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void returnsNullCoverPhotoWhenLogHasNoPhotos() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		insertUser(7L, "writer");

		Meeting meeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		insertLog(meeting.getId(), 7L, "No photo log.", "SUCCESS", "2026-04-15T01:00:00Z");

		entityManager.flush();
		entityManager.clear();

		CrewMeetingLogFeedView result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).coverPhotoUrl()).isNull();
		assertThat(result.items().get(0).extraPhotoCount()).isZero();
	}

	@Test
	void excludesSoftDeletedLogs() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		insertUser(7L, "writer");

		Meeting meeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (
				meeting_id, author_user_id, body, result, created_at, updated_at,
				deleted_at, deleted_by_user_id, delete_reason
			)
			values (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""")
			.setParameter(1, meeting.getId())
			.setParameter(2, 7L)
			.setParameter(3, "deleted log")
			.setParameter(4, "FAILURE")
			.setParameter(5, timestamp("2026-04-15T01:00:00Z"))
			.setParameter(6, timestamp("2026-04-15T01:00:00Z"))
			.setParameter(7, timestamp("2026-04-16T01:00:00Z"))
			.setParameter(8, 7L)
			.setParameter(9, "deleted by moderator")
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		CrewMeetingLogFeedView result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).isEmpty();
	}

	@Test
	void returnsHasNextWhenMoreLogsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		insertUser(7L, "writer");

		Meeting firstMeeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		Meeting secondMeeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 2", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));

		insertLog(firstMeeting.getId(), 7L, "first log", "SUCCESS", "2026-04-14T01:00:00Z");
		insertLog(secondMeeting.getId(), 7L, "second log", "FAILURE", "2026-04-15T01:00:00Z");

		entityManager.flush();
		entityManager.clear();

		CrewMeetingLogFeedView result = repository.search(crew.getId(), 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.page().hasNext()).isTrue();
	}

	private void insertUser(Long id, String nickname) {
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (?, ?, now(), now())")
			.setParameter(1, id)
			.setParameter(2, nickname)
			.executeUpdate();
	}

	private void insertLog(Long meetingId, Long authorUserId, String body, String result, String createdAt) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, result, created_at, updated_at)
			values (?, ?, ?, ?, ?, ?)
			""")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.setParameter(3, body)
			.setParameter(4, result)
			.setParameter(5, timestamp(createdAt))
			.setParameter(6, timestamp(createdAt))
			.executeUpdate();
	}

	private Long findLogId(Long meetingId) {
		return ((Number) entityManager.getEntityManager()
			.createNativeQuery("select id from meeting_logs where meeting_id = ?")
			.setParameter(1, meetingId)
			.getSingleResult()).longValue();
	}

	private java.sql.Timestamp timestamp(String value) {
		return java.sql.Timestamp.from(java.time.Instant.parse(value));
	}
}
