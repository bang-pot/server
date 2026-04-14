package com.bangpot.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.meeting.application.port.ArchivedMeetingReadRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;

@DataJpaTest
@Import(JpaArchivedMeetingReadRepository.class)
class JpaArchivedMeetingReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ArchivedMeetingReadRepository repository;

	@Test
	void returnsOnlyCompletedMeetingsRelatedToCurrentUser() {
		Crew activeCrew = entityManager.persist(Crew.create("Active Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persist(deletedCrew);

		Meeting hosted = entityManager.persist(Meeting.create(
			activeCrew.getId(), 7L, "Hosted Escape", "Deep Blue", "Hongdae", "2026-04-12", "20:00", 4, null, null, null
		));
		hosted.closeRecruitment();
		hosted.complete();

		Meeting joined = entityManager.persist(Meeting.create(
			activeCrew.getId(), 8L, "Joined Escape", "Time Attack", "Gangnam", "2026-04-11", "19:00", 4, null, null, null
		));
		joined.closeRecruitment();
		joined.complete();
		entityManager.persist(MeetingParticipant.join(joined.getId(), 7L));

		Meeting left = entityManager.persist(Meeting.create(
			deletedCrew.getId(), 9L, "Left Escape", "Lost Harbor", "Busan", "2026-04-10", "18:00", 4, null, null, null
		));
		left.closeRecruitment();
		left.complete();
		MeetingParticipant leftParticipant = entityManager.persist(MeetingParticipant.join(left.getId(), 7L));
		leftParticipant.leave();

		Meeting legacyApproved = entityManager.persist(Meeting.create(
			activeCrew.getId(), 10L, "Legacy Escape", "Code Red", "Mapo", "2026-04-09", "17:00", 4, null, null, null
		));
		legacyApproved.closeRecruitment();
		legacyApproved.complete();
		entityManager.persist(MeetingParticipant.rehydrate(null, legacyApproved.getId(), 7L,
			com.bangpot.meeting.domain.MeetingParticipationStatus.APPROVED, null, null));

		Meeting recruiting = entityManager.persist(Meeting.create(
			activeCrew.getId(), 7L, "Recruiting Escape", "Another", "Mapo", "2026-04-20", "21:00", 4, null, null, null
		));
		entityManager.persist(MeetingParticipant.join(recruiting.getId(), 7L));

		Meeting unrelated = entityManager.persist(Meeting.create(
			activeCrew.getId(), 11L, "Unrelated Escape", "Hidden", "Seoul", "2026-04-08", "16:00", 4, null, null, null
		));
		unrelated.closeRecruitment();
		unrelated.complete();

		entityManager.flush();
		entityManager.clear();

		ArchivedMeetingReadRepository.SearchResult result = repository.search(7L, 0, 10);

		assertThat(result.items()).extracting(ArchivedMeetingReadRepository.Item::meetingId)
			.containsExactly(hosted.getId(), joined.getId(), left.getId(), legacyApproved.getId());
		assertThat(result.items()).extracting(ArchivedMeetingReadRepository.Item::crewName)
			.contains("Deleted Crew");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}
}

