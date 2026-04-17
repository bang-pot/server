package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.port.CreatedMeetingReadRepository;

@DataJpaTest
class JpaCreatedMeetingReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CreatedMeetingReadRepository repository;

	@Test
	void returnsOnlyCurrentUsersHostedMeetingsInDescendingScheduleOrder() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting olderMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Older Meeting", "Theme A", "Hongdae", "2026-04-10", "18:00", 4, null, null, null
		));
		Meeting sameDateEarlierTime = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Earlier Time", "Theme B", "Gangnam", "2026-04-12", "17:00", 4, null, null, null
		));
		Meeting sameDateLaterTime = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Later Time", "Theme C", "Seongsu", "2026-04-12", "20:00", 4, null, null, null
		));
		Meeting canceledMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Canceled Meeting", "Theme D", "Yeonnam", "2026-04-13", "16:00", 4, null, null, null
		));
		canceledMeeting.cancel();
		entityManager.persistAndFlush(canceledMeeting);
		entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Other Host Meeting", "Theme E", "Jamsil", "2026-04-14", "19:00", 4, null, null, null
		));
		entityManager.persistAndFlush(Meeting.create(
			deletedCrew.getId(), 7L, "Deleted Crew Meeting", "Theme F", "Mapo", "2026-04-15", "19:00", 4, null, null, null
		));

		entityManager.flush();
		entityManager.clear();

		CreatedMeetingReadRepository.SearchResult result = repository.search(7L, 0, 10);

		assertThat(result.items()).extracting(CreatedMeetingReadRepository.Item::meetingId)
			.containsExactly(canceledMeeting.getId(), sameDateLaterTime.getId(), sameDateEarlierTime.getId(), olderMeeting.getId());
		assertThat(result.items()).extracting(CreatedMeetingReadRepository.Item::crewName)
			.containsOnly("Alpha Crew");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMoreMeetingsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "First", "Theme A", "Hongdae", "2026-04-13", "20:00", 4, null, null, null
		));
		entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Second", "Theme B", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));

		entityManager.flush();
		entityManager.clear();

		CreatedMeetingReadRepository.SearchResult result = repository.search(7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}
}
