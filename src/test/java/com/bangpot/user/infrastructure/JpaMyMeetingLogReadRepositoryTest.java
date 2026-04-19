package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogDeletedBy;
import com.bangpot.meeting.domain.MeetingLogPhoto;
import com.bangpot.user.application.port.MyMeetingLogReadRepository;

@DataJpaTest
class JpaMyMeetingLogReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MyMeetingLogReadRepository repository;

	@Test
	void returnsOnlyActiveAuthoredLogsOnActiveCrewsWithCoverPhotoAndPhotoCount() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting activeMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Friday Escape", "Theme A", "Hongdae", "2026-04-18", "19:00", 4, null, null, null
		));
		Meeting newerMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Saturday Escape", "Theme B", "Seongsu", "2026-04-19", "20:00", 4, null, null, null
		));
		Meeting deletedCrewMeeting = entityManager.persistAndFlush(Meeting.create(
			deletedCrew.getId(), 7L, "Deleted Crew Escape", "Theme C", "Mapo", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting deletedLogMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Deleted Log Escape", "Theme D", "Mapo", "2026-04-17", "18:00", 4, null, null, null
		));

		MeetingLog includedLog = entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			activeMeeting.getId(),
			7L,
			"Included body",
			Instant.parse("2026-04-19T10:15:30Z"),
			Instant.parse("2026-04-19T10:15:30Z"),
			null,
			null,
			null,
			null
		));
		MeetingLog newerLog = entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			newerMeeting.getId(),
			7L,
			"Newer body",
			Instant.parse("2026-04-20T09:00:00Z"),
			Instant.parse("2026-04-20T09:00:00Z"),
			null,
			null,
			null,
			null
		));
		entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			activeMeeting.getId(),
			8L,
			"Other author body",
			Instant.parse("2026-04-18T09:00:00Z"),
			Instant.parse("2026-04-18T09:00:00Z"),
			null,
			null,
			null,
			null
		));
		entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			deletedLogMeeting.getId(),
			7L,
			"Deleted body",
			Instant.parse("2026-04-17T09:00:00Z"),
			Instant.parse("2026-04-17T09:30:00Z"),
			Instant.parse("2026-04-18T00:00:00Z"),
			7L,
			MeetingLogDeletedBy.AUTHOR,
			null
		));
		entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			deletedCrewMeeting.getId(),
			7L,
			"Deleted crew body",
			Instant.parse("2026-04-16T09:00:00Z"),
			Instant.parse("2026-04-16T09:00:00Z"),
			null,
			null,
			null,
			null
		));

		entityManager.persistAndFlush(MeetingLogPhoto.create(includedLog.getId(), "https://cdn.example.com/second.jpg", Instant.parse("2026-04-19T10:20:00Z")));
		entityManager.persistAndFlush(MeetingLogPhoto.create(includedLog.getId(), "https://cdn.example.com/first.jpg", Instant.parse("2026-04-19T10:16:00Z")));
		entityManager.persistAndFlush(MeetingLogPhoto.create(newerLog.getId(), "https://cdn.example.com/newer.jpg", Instant.parse("2026-04-20T09:05:00Z")));

		entityManager.flush();
		entityManager.clear();

		MyMeetingLogReadRepository.SearchResult result = repository.search(7L, 0, 20);

		assertThat(result.items()).extracting(MyMeetingLogReadRepository.Item::logId)
			.containsExactly(newerLog.getId(), includedLog.getId());
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/newer.jpg");
		assertThat(result.items().get(0).photoCount()).isEqualTo(1L);
		assertThat(result.items().get(1).coverPhotoUrl()).isEqualTo("https://cdn.example.com/second.jpg");
		assertThat(result.items().get(1).photoCount()).isEqualTo(2L);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void sortsMyMeetingLogsByCreatedAtAndLogIdDescending() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting firstMeeting = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Friday Escape", "Theme A", "Hongdae", "2026-04-18", "19:00", 4, null, null, null
		));
		Meeting secondMeeting = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Saturday Escape", "Theme B", "Seongsu", "2026-04-19", "20:00", 4, null, null, null
		));

		MeetingLog first = entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			firstMeeting.getId(),
			7L,
			"First body",
			Instant.parse("2026-04-19T10:00:00Z"),
			Instant.parse("2026-04-19T10:00:00Z"),
			null,
			null,
			null,
			null
		));
		MeetingLog second = entityManager.persistAndFlush(MeetingLog.rehydrate(
			null,
			secondMeeting.getId(),
			7L,
			"Second body",
			Instant.parse("2026-04-19T10:00:00Z"),
			Instant.parse("2026-04-19T10:00:00Z"),
			null,
			null,
			null,
			null
		));

		entityManager.flush();
		entityManager.clear();

		MyMeetingLogReadRepository.SearchResult result = repository.search(7L, 0, 20);

		assertThat(result.items()).extracting(MyMeetingLogReadRepository.Item::logId)
			.containsExactly(second.getId(), first.getId());
	}
}
