package com.bangpot.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingLogFeedReadRepository;
import com.bangpot.meeting.domain.Meeting;

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
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'writer', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'writer2', now(), now())")
			.executeUpdate();

		Meeting meeting1 = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		Meeting meeting2 = entityManager.persist(Meeting.create(
			crew.getId(), 8L, "Friday 2", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));

		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, ?, ?)
			""")
			.setParameter(1, meeting1.getId())
			.setParameter(2, 7L)
			.setParameter(3, "First log body for feed card.")
			.setParameter(4, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-14T01:00:00Z")))
			.setParameter(5, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-14T01:00:00Z")))
			.executeUpdate();
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, ?, ?)
			""")
			.setParameter(1, meeting2.getId())
			.setParameter(2, 8L)
			.setParameter(3, "Second log body.")
			.setParameter(4, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.setParameter(5, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.executeUpdate();

		Long log1Id = ((Number) entityManager.getEntityManager()
			.createNativeQuery("select id from meeting_logs where meeting_id = ?")
			.setParameter(1, meeting1.getId())
			.getSingleResult()).longValue();
		Long log2Id = ((Number) entityManager.getEntityManager()
			.createNativeQuery("select id from meeting_logs where meeting_id = ?")
			.setParameter(1, meeting2.getId())
			.getSingleResult()).longValue();

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

		MeetingLogFeedReadRepository.SearchResult result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).extracting(MeetingLogFeedReadRepository.Item::meetingId)
			.containsExactly(meeting2.getId(), meeting1.getId());
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/c.jpg");
		assertThat(result.items().get(0).totalPhotoCount()).isEqualTo(1);
		assertThat(result.items().get(1).coverPhotoUrl()).isEqualTo("https://cdn.example.com/a.jpg");
		assertThat(result.items().get(1).totalPhotoCount()).isEqualTo(2);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsNullCoverPhotoWhenLogHasNoPhotos() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'writer', now(), now())")
			.executeUpdate();

		Meeting meeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, ?, ?)
			""")
			.setParameter(1, meeting.getId())
			.setParameter(2, 7L)
			.setParameter(3, "No photo log.")
			.setParameter(4, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.setParameter(5, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		MeetingLogFeedReadRepository.SearchResult result = repository.search(crew.getId(), 0, 10);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).coverPhotoUrl()).isNull();
		assertThat(result.items().get(0).totalPhotoCount()).isZero();
	}

	@Test
	void returnsHasNextWhenMoreLogsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'writer', now(), now())")
			.executeUpdate();

		Meeting firstMeeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 1", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		Meeting secondMeeting = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday 2", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));

		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, 7, 'first log', ?, ?)
			""")
			.setParameter(1, firstMeeting.getId())
			.setParameter(2, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-14T01:00:00Z")))
			.setParameter(3, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-14T01:00:00Z")))
			.executeUpdate();
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, 7, 'second log', ?, ?)
			""")
			.setParameter(1, secondMeeting.getId())
			.setParameter(2, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.setParameter(3, java.sql.Timestamp.from(java.time.Instant.parse("2026-04-15T01:00:00Z")))
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		MeetingLogFeedReadRepository.SearchResult result = repository.search(crew.getId(), 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}
}
