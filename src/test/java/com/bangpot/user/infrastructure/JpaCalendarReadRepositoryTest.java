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
import com.bangpot.user.application.port.CalendarReadRepository;

@DataJpaTest
class JpaCalendarReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CalendarReadRepository repository;

	@Test
	void returnsHostedAndConfirmedParticipantMeetingsIncludingCanceledOnActiveCrews() {
		Crew activeCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting hostRecruiting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 7L, "Host Recruiting", "Theme A", "Hongdae", "2026-04-20", "19:00", 4, null, null, null
		));
		Meeting approvedClosed = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Approved Closed", "Theme B", "Gangnam", "2026-04-21", "18:00", 4, null, null, null
		));
		approvedClosed.closeRecruitment();
		entityManager.persistAndFlush(approvedClosed);
		Meeting joinedCanceled = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Joined Canceled", "Theme C", "Seongsu", "2026-04-22", "17:00", 4, null, null, null
		));
		joinedCanceled.cancel();
		entityManager.persistAndFlush(joinedCanceled);
		Meeting pendingMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Pending Meeting", "Theme D", "Jamsil", "2026-04-23", "16:00", 4, null, null, null
		));
		Meeting leftMeeting = entityManager.persistAndFlush(Meeting.create(
			activeCrew.getId(), 8L, "Left Meeting", "Theme E", "Mapo", "2026-04-24", "15:00", 4, null, null, null
		));
		Meeting deletedCrewMeeting = entityManager.persistAndFlush(Meeting.create(
			deletedCrew.getId(), 7L, "Deleted Crew Meeting", "Theme F", "Mapo", "2026-04-25", "14:00", 4, null, null, null
		));

		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, approvedClosed.getId(), 7L, MeetingParticipationStatus.APPROVED, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(joinedCanceled.getId(), 7L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, pendingMeeting.getId(), 7L, MeetingParticipationStatus.PENDING, null, null)
		);
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, leftMeeting.getId(), 7L, MeetingParticipationStatus.LEFT, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(deletedCrewMeeting.getId(), 7L));

		entityManager.flush();
		entityManager.clear();

		CalendarReadRepository.View result = repository.load(7L);

		assertThat(result.items()).extracting(CalendarReadRepository.Item::meetingId)
			.containsExactly(
				hostRecruiting.getId(),
				approvedClosed.getId(),
				joinedCanceled.getId()
			);
		assertThat(result.items()).extracting(CalendarReadRepository.Item::participationRole)
			.containsExactly("HOST", "PARTICIPANT", "PARTICIPANT");
		assertThat(result.items()).extracting(CalendarReadRepository.Item::isCanceled)
			.containsExactly(false, false, true);
		assertThat(result.totalCount()).isEqualTo(3);
	}

	@Test
	void sortsCalendarItemsByDateTimeAndMeetingIdAscending() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		Meeting first = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "First", "Theme A", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting second = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 8L, "Second", "Theme B", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting third = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 8L, "Third", "Theme C", "Hongdae", "2026-04-20", "19:00", 4, null, null, null
		));

		entityManager.persistAndFlush(MeetingParticipant.join(second.getId(), 7L));
		entityManager.persistAndFlush(MeetingParticipant.join(third.getId(), 7L));

		entityManager.flush();
		entityManager.clear();

		CalendarReadRepository.View result = repository.load(7L);

		assertThat(result.items()).extracting(CalendarReadRepository.Item::meetingId)
			.containsExactly(first.getId(), second.getId(), third.getId());
	}
}
