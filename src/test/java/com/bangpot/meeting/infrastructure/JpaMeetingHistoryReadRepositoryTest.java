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

@DataJpaTest
@Import(JpaMeetingHistoryReadRepository.class)
class JpaMeetingHistoryReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingHistoryReadRepository repository;

	@Test
	void returnsOnlyCompletedMeetingsWithCurrentUsersLogState() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew otherCrew = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));

		Meeting latestCompleted = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "금요일 이스케이프", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		latestCompleted.closeRecruitment();
		latestCompleted.complete();

		Meeting olderCompleted = entityManager.persist(Meeting.create(
			crew.getId(), 8L, "토요일 이스케이프", "Lost Harbor", "Busan", "2026-04-11", "19:00", 4, null, null, null
		));
		olderCompleted.closeRecruitment();
		olderCompleted.complete();

		Meeting recruiting = entityManager.persist(Meeting.create(
			crew.getId(), 9L, "모집 중 이스케이프", "Time Attack", "Gangnam", "2026-04-20", "18:00", 4, null, null, null
		));

		Meeting otherCrewCompleted = entityManager.persist(Meeting.create(
			otherCrew.getId(), 10L, "다른 크루 이스케이프", "Code Red", "Mapo", "2026-04-10", "18:00", 4, null, null, null
		));
		otherCrewCompleted.closeRecruitment();
		otherCrewCompleted.complete();

		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, ?, ?)
			""")
			.setParameter(1, latestCompleted.getId())
			.setParameter(2, 7L)
			.setParameter(3, "내 로그가 있는 완료 모임")
			.setParameter(4, Timestamp.from(Instant.parse("2026-04-15T01:00:00Z")))
			.setParameter(5, Timestamp.from(Instant.parse("2026-04-15T01:00:00Z")))
			.executeUpdate();

		entityManager.flush();
		entityManager.clear();

		MeetingHistoryReadRepository.SearchResult result = repository.search(crew.getId(), 7L, 0, 10);

		assertThat(result.items()).extracting(MeetingHistoryReadRepository.Item::meetingId)
			.containsExactly(latestCompleted.getId(), olderCompleted.getId());
		assertThat(result.items().get(0).logId()).isNotNull();
		assertThat(result.items().get(1).logId()).isNull();
		assertThat(result.items()).extracting(MeetingHistoryReadRepository.Item::meetingTitle)
			.doesNotContain("모집 중 이스케이프", "다른 크루 이스케이프");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMoreCompletedMeetingsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		Meeting first = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "첫 번째", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		first.closeRecruitment();
		first.complete();

		Meeting second = entityManager.persist(Meeting.create(
			crew.getId(), 7L, "두 번째", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
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
