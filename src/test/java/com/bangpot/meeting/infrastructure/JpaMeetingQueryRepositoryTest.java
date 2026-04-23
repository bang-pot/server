package com.bangpot.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

@DataJpaTest
@Import(JpaMeetingQueryRepository.class)
class JpaMeetingQueryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingQueryRepository repository;

	@Test
	void countsCompletedMeetingsForHostsAndJoinedParticipantsInBatch() {
		Meeting hostedCompleted = entityManager.persist(Meeting.create(
			1L, 7L, "hosted", "Theme A", "Seoul", "2026-04-20", "10:00", 4, null, null, "desc"
		));
		hostedCompleted.closeRecruitment();
		hostedCompleted.complete();

		Meeting joinedCompleted = entityManager.persist(Meeting.create(
			1L, 20L, "joined", "Theme B", "Seoul", "2026-04-21", "11:00", 4, null, null, "desc"
		));
		joinedCompleted.closeRecruitment();
		joinedCompleted.complete();
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			joinedCompleted.getId(),
			7L,
			MeetingParticipationStatus.APPROVED,
			null,
			null
		));

		Meeting pendingCompleted = entityManager.persist(Meeting.create(
			1L, 21L, "pending", "Theme C", "Seoul", "2026-04-22", "12:00", 4, null, null, "desc"
		));
		pendingCompleted.closeRecruitment();
		pendingCompleted.complete();
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			pendingCompleted.getId(),
			8L,
			MeetingParticipationStatus.PENDING,
			null,
			null
		));

		Meeting leftCompleted = entityManager.persist(Meeting.create(
			1L, 22L, "left", "Theme D", "Seoul", "2026-04-23", "13:00", 4, null, null, "desc"
		));
		leftCompleted.closeRecruitment();
		leftCompleted.complete();
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			leftCompleted.getId(),
			9L,
			MeetingParticipationStatus.LEFT,
			null,
			null
		));

		Meeting recruiting = entityManager.persist(Meeting.create(
			1L, 7L, "recruiting", "Theme E", "Seoul", "2026-04-24", "14:00", 4, null, null, "desc"
		));
		entityManager.persist(MeetingParticipant.join(recruiting.getId(), 8L));

		entityManager.flush();
		entityManager.clear();

		Map<Long, Integer> result = repository.countCompletedByUserIds(List.of(7L, 8L, 9L, 10L));

		assertThat(result).containsEntry(7L, 2);
		assertThat(result).containsEntry(8L, 1);
		assertThat(result).doesNotContainKeys(9L, 10L);
	}
}
