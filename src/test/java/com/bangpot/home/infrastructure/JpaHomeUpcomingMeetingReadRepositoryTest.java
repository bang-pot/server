package com.bangpot.home.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.home.application.port.HomeUpcomingMeetingReadRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

@DataJpaTest
@Import(JpaHomeUpcomingMeetingReadRepositoryAdapter.class)
class JpaHomeUpcomingMeetingReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private HomeUpcomingMeetingReadRepository repository;

	@Test
	void returnsUpcomingMeetingsForHostAndJoinedApprovedPendingParticipantsOrderedAscending() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting hostRecruiting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Host Recruiting", "Theme A", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting approvedMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Approved Meeting", "Theme B", "Hongdae", "2026-04-20", "19:00", 4, null, null, null
		));
		Meeting joinedMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Joined Meeting", "Theme C", "Hongdae", "2026-04-21", "17:00", 4, null, null, null
		));
		Meeting pendingMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Pending Meeting", "Theme D", "Hongdae", "2026-04-21", "18:00", 4, null, null, null
		));
		Meeting sameDayPastMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Past Same Day", "Theme E", "Hongdae", "2026-04-19", "09:00", 4, null, null, null
		));
		Meeting canceledMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Canceled Meeting", "Theme F", "Hongdae", "2026-04-22", "18:00", 4, null, null, null
		));
		canceledMeeting.cancel();
		entityManager.persistAndFlush(canceledMeeting);
		Meeting completedMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Completed Meeting", "Theme G", "Hongdae", "2026-04-23", "18:00", 4, null, null, null
		));
		completedMeeting.closeRecruitment();
		completedMeeting.complete();
		entityManager.persistAndFlush(completedMeeting);
		Meeting deletedCrewMeeting = entityManager.persistAndFlush(Meeting.create(
			deletedCrew.getId(), 8L, "Deleted Crew Meeting", "Theme H", "Hongdae", "2026-04-24", "18:00", 4, null, null, null
		));
		Meeting leftMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Left Meeting", "Theme I", "Hongdae", "2026-04-25", "18:00", 4, null, null, null
		));

		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, approvedMeeting.getId(), 7L, MeetingParticipationStatus.APPROVED, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(joinedMeeting.getId(), 7L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, pendingMeeting.getId(), 7L, MeetingParticipationStatus.PENDING, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(sameDayPastMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(canceledMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(completedMeeting.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(deletedCrewMeeting.getId(), 7L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, leftMeeting.getId(), 7L, MeetingParticipationStatus.LEFT, null, null)
		);

		entityManager.clear();

		var result = repository.findUpcomingMeetings(7L, 10, "2026-04-19", "10:00");

		assertThat(result.items()).extracting(HomeUpcomingMeetingReadRepository.Item::meetingId)
			.containsExactly(
				hostRecruiting.getId(),
				approvedMeeting.getId(),
				joinedMeeting.getId(),
				pendingMeeting.getId()
			);
		assertThat(result.items()).extracting(HomeUpcomingMeetingReadRepository.Item::status)
			.containsExactly("RECRUITING", "RECRUITING", "RECRUITING", "RECRUITING");
		assertThat(result.totalCount()).isEqualTo(4L);
	}

	@Test
	void limitsUpcomingMeetingsPreviewButKeepsTotalCount() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting first = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "First", "Theme A", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting second = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Second", "Theme B", "Hongdae", "2026-04-21", "18:00", 4, null, null, null
		));
		entityManager.persistAndFlush(MeetingParticipant.join(first.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(second.getId(), 7L));

		entityManager.clear();

		var result = repository.findUpcomingMeetings(7L, 1, "2026-04-19", "10:00");

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(first.getId());
		assertThat(result.totalCount()).isEqualTo(2L);
	}
}
