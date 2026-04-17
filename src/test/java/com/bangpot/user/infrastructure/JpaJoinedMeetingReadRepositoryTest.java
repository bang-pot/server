package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.user.application.port.JoinedMeetingReadRepository;

@DataJpaTest
class JpaJoinedMeetingReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private JoinedMeetingReadRepository repository;

	@Test
	void returnsOnlyJoinedNonHostMeetingsAndComputesReviewWriteState() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting completedWritable = completedMeeting(activeCrew.getId(), 8L, "2026-04-18", "19:00", "Review Writable");
		Meeting completedWithActiveLog = completedMeeting(activeCrew.getId(), 8L, "2026-04-17", "19:00", "Review Exists");
		Meeting completedDeletedBlocked = completedMeeting(activeCrew.getId(), 8L, "2026-04-16", "19:00", "Review Blocked");
		Meeting recruitingMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Recruiting Meeting", "Theme D", "Hongdae", "2026-04-19", "18:00", 4, null, null, null
		));
		Meeting canceledMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Canceled Meeting", "Theme E", "Hongdae", "2026-04-15", "18:00", 4, null, null, null
		));
		canceledMeeting.cancel();
		entityManager.persistAndFlush(canceledMeeting);
		Meeting hostMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Hosted By Me", "Theme F", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting leftMeeting = completedMeeting(activeCrew.getId(), 8L, "2026-04-14", "18:00", "Left Meeting");
		Meeting deletedCrewMeeting = completedMeeting(deletedCrew.getId(), 8L, "2026-04-13", "18:00", "Deleted Crew Meeting");

		entityManager.persistAndFlush(MeetingParticipant.join(completedWritable.getId(), 7L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, completedWithActiveLog.getId(), 7L, MeetingParticipationStatus.APPROVED, null, null)
		);
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, completedDeletedBlocked.getId(), 7L, MeetingParticipationStatus.PENDING, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(recruitingMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(canceledMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(hostMeeting.getId(), 7L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, leftMeeting.getId(), 7L, MeetingParticipationStatus.LEFT, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(deletedCrewMeeting.getId(), 7L));

		insertActiveLog(completedWithActiveLog.getId(), 7L, "active log");
		insertDeletedLog(completedDeletedBlocked.getId(), 7L, "deleted log");

		entityManager.flush();
		entityManager.clear();

		JoinedMeetingReadRepository.SearchResult result = repository.search(7L, 0, 10);

		assertThat(result.items()).extracting(JoinedMeetingReadRepository.Item::meetingId)
			.containsExactly(
				recruitingMeeting.getId(),
				completedWritable.getId(),
				completedWithActiveLog.getId(),
				completedDeletedBlocked.getId(),
				canceledMeeting.getId()
			);
		assertThat(result.items()).extracting(JoinedMeetingReadRepository.Item::canWriteReview)
			.containsExactly(false, true, false, false, false);
		assertThat(result.items()).extracting(JoinedMeetingReadRepository.Item::result)
			.containsExactly(null, "NOT_RECORDED", "NOT_RECORDED", "NOT_RECORDED", null);
		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(hostMeeting.getId()));
		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(leftMeeting.getId()));
		assertThat(result.items()).noneMatch(item -> item.meetingId().equals(deletedCrewMeeting.getId()));
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMoreJoinedMeetingsExistThanRequestedSize() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting newerMeeting = completedMeeting(crew.getId(), 8L, "2026-04-18", "19:00", "First");
		Meeting olderMeeting = completedMeeting(crew.getId(), 8L, "2026-04-17", "19:00", "Second");

		entityManager.persistAndFlush(MeetingParticipant.join(newerMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(olderMeeting.getId(), 7L));

		entityManager.flush();
		entityManager.clear();

		JoinedMeetingReadRepository.SearchResult result = repository.search(7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}

	private Meeting completedMeeting(Long crewId, Long hostUserId, String date, String time, String title) {
		Meeting meeting = entityManager.persist(Meeting.create(
			crewId, hostUserId, title, "Deep Blue", "Hongdae", date, time, 4, null, null, null
		));
		meeting.closeRecruitment();
		meeting.complete();
		return entityManager.persistAndFlush(meeting);
	}

	private void insertActiveLog(Long meetingId, Long authorUserId, String body) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into meeting_logs (meeting_id, author_user_id, body, created_at, updated_at)
			values (?, ?, ?, now(), now())
			""")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.setParameter(3, body)
			.executeUpdate();
	}

	private void insertDeletedLog(Long meetingId, Long authorUserId, String body) {
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
			values (?, ?, ?, now(), now(), now(), ?, 'AUTHOR', null)
			""")
			.setParameter(1, meetingId)
			.setParameter(2, authorUserId)
			.setParameter(3, body)
			.setParameter(4, authorUserId)
			.executeUpdate();
	}
}
