package com.bangpot.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingHistoryReadRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogPhoto;

@DataJpaTest
@Import(JpaMeetingHistoryReadRepository.class)
class JpaMeetingHistoryReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingHistoryReadRepository repository;

	@Test
	void returnsOnlyCompletedMeetingsWithRicherHistoryFields() {
		String longSummary = "L".repeat(130);
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew otherCrew = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));

		Meeting latestCompleted = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Friday Escape", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		latestCompleted.closeRecruitment();
		latestCompleted.complete();

		Meeting olderCompleted = entityManager.persist(Meeting.create(
			crew.getId(), 8L, "Saturday Harbor", "Lost Harbor", "Busan", "2026-04-11", "19:00", 4, null, null, null
		));
		olderCompleted.closeRecruitment();
		olderCompleted.complete();

		Meeting recruiting = entityManager.persist(Meeting.create(
			crew.getId(), 9L, "Recruiting Escape", "Time Attack", "Gangnam", "2026-04-20", "18:00", 4, null, null, null
		));

		Meeting otherCrewCompleted = entityManager.persist(Meeting.create(
			otherCrew.getId(), 10L, "Other Crew Escape", "Code Red", "Mapo", "2026-04-10", "18:00", 4, null, null, null
		));
		otherCrewCompleted.closeRecruitment();
		otherCrewCompleted.complete();

		entityManager.persistAndFlush(MeetingLog.create(
			latestCompleted.getId(),
			7L,
			"My own log.",
			Instant.parse("2026-04-15T01:00:00Z")
		));
		MeetingLog latestLog = entityManager.persistAndFlush(MeetingLog.create(
			latestCompleted.getId(),
			8L,
			longSummary,
			Instant.parse("2026-04-15T02:00:00Z")
		));
		MeetingLog olderLog = entityManager.persistAndFlush(MeetingLog.create(
			olderCompleted.getId(),
			8L,
			"Older completed meeting review body.",
			Instant.parse("2026-04-15T00:30:00Z")
		));

		entityManager.persistAndFlush(MeetingLogPhoto.create(
			latestLog.getId(),
			"https://cdn.example.com/latest-cover-1.jpg",
			Instant.parse("2026-04-15T02:00:00Z")
		));
		entityManager.persistAndFlush(MeetingLogPhoto.create(
			latestLog.getId(),
			"https://cdn.example.com/latest-cover-2.jpg",
			Instant.parse("2026-04-15T02:00:01Z")
		));
		entityManager.persistAndFlush(MeetingLogPhoto.create(
			olderLog.getId(),
			"https://cdn.example.com/older-cover.jpg",
			Instant.parse("2026-04-15T00:30:00Z")
		));

		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_participation_requests (meeting_id, user_id, status, created_at, updated_at)
			values (?, ?, ?, ?, ?), (?, ?, ?, ?, ?), (?, ?, ?, ?, ?), (?, ?, ?, ?, ?)
			""")
			.setParameter(1, latestCompleted.getId())
			.setParameter(2, 20L)
			.setParameter(3, "JOINED")
			.setParameter(4, Timestamp.from(Instant.parse("2026-04-14T01:00:00Z")))
			.setParameter(5, Timestamp.from(Instant.parse("2026-04-14T01:00:00Z")))
			.setParameter(6, latestCompleted.getId())
			.setParameter(7, 21L)
			.setParameter(8, "APPROVED")
			.setParameter(9, Timestamp.from(Instant.parse("2026-04-14T01:10:00Z")))
			.setParameter(10, Timestamp.from(Instant.parse("2026-04-14T01:10:00Z")))
			.setParameter(11, latestCompleted.getId())
			.setParameter(12, 22L)
			.setParameter(13, "LEFT")
			.setParameter(14, Timestamp.from(Instant.parse("2026-04-14T01:20:00Z")))
			.setParameter(15, Timestamp.from(Instant.parse("2026-04-14T01:20:00Z")))
			.setParameter(16, olderCompleted.getId())
			.setParameter(17, 23L)
			.setParameter(18, "PENDING")
			.setParameter(19, Timestamp.from(Instant.parse("2026-04-14T01:30:00Z")))
			.setParameter(20, Timestamp.from(Instant.parse("2026-04-14T01:30:00Z")))
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		MeetingHistoryReadRepository.SearchResult result = repository.search(crew.getId(), 7L, 0, 10);

		assertThat(result.items()).extracting(MeetingHistoryReadRepository.Item::meetingId)
			.containsExactly(latestCompleted.getId(), olderCompleted.getId());

		MeetingHistoryReadRepository.Item latestItem = result.items().get(0);
		assertThat(latestItem.logId()).isNotNull();
		assertThat(latestItem.reviewSummary()).isEqualTo(longSummary.substring(0, 120));
		assertThat(latestItem.reviewSummary()).hasSize(120);
		assertThat(latestItem.logCount()).isEqualTo(2L);
		assertThat(latestItem.participantCount()).isEqualTo(3L);
		assertThat(latestItem.coverPhotoUrl()).isEqualTo("https://cdn.example.com/latest-cover-1.jpg");

		MeetingHistoryReadRepository.Item olderItem = result.items().get(1);
		assertThat(olderItem.logId()).isNull();
		assertThat(olderItem.reviewSummary()).isEqualTo("Older completed meeting review body.");
		assertThat(olderItem.logCount()).isEqualTo(1L);
		assertThat(olderItem.participantCount()).isEqualTo(2L);
		assertThat(olderItem.coverPhotoUrl()).isEqualTo("https://cdn.example.com/older-cover.jpg");

		assertThat(result.items()).extracting(MeetingHistoryReadRepository.Item::meetingTitle)
			.doesNotContain("Recruiting Escape", "Other Crew Escape");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMoreCompletedMeetingsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		Meeting first = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "First", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		first.closeRecruitment();
		first.complete();

		Meeting second = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "Second", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));
		second.closeRecruitment();
		second.complete();

		entityManager.flush();
		entityManager.clear();

		MeetingHistoryReadRepository.SearchResult result = repository.search(crew.getId(), 7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}
}
