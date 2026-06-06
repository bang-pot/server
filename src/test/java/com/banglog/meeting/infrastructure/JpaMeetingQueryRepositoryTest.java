package com.banglog.meeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingLog;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingResult;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.meeting.domain.view.CrewScheduleView;
import com.banglog.meeting.domain.view.MeetingsView;

@DataJpaTest
@Import(JpaMeetingQueryRepository.class)
class JpaMeetingQueryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MeetingQueryRepository repository;

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

		var result = repository.findUpcomingMeetingsViewByUserId(7L, 10, "2026-04-19", "10:00");

		assertThat(result.nearestMeeting().meetingId()).isEqualTo(hostRecruiting.getId());
		assertThat(result.nearestMeeting().themeName()).isEqualTo("Theme A");
		assertThat(result.totalCount()).isEqualTo(4L);
	}

	@Test
	void returnsActivityRecordWithCompletedCountAndSuccessCount() {
		Meeting hostedSuccess = entityManager.persist(Meeting.create(
			1L, 7L, "hosted success", "Theme A", "Seoul", "2026-04-20", "10:00", 4, null, null, "desc"
		));
		hostedSuccess.closeRecruitment();
		hostedSuccess.complete();
		entityManager.persist(MeetingLog.create(hostedSuccess.getId(), 7L, "great", MeetingResult.SUCCESS, Instant.parse("2026-04-20T12:00:00Z")));

		Meeting hostedFailure = entityManager.persist(Meeting.create(
			1L, 7L, "hosted failure", "Theme B", "Seoul", "2026-04-21", "11:00", 4, null, null, "desc"
		));
		hostedFailure.closeRecruitment();
		hostedFailure.complete();
		entityManager.persist(MeetingLog.create(hostedFailure.getId(), 7L, "hard", MeetingResult.FAILURE, Instant.parse("2026-04-21T12:00:00Z")));

		Meeting joinedSuccess = entityManager.persist(Meeting.create(
			1L, 20L, "joined success", "Theme C", "Seoul", "2026-04-22", "12:00", 4, null, null, "desc"
		));
		joinedSuccess.closeRecruitment();
		joinedSuccess.complete();
		entityManager.persist(MeetingLog.create(joinedSuccess.getId(), 7L, "success", MeetingResult.SUCCESS, Instant.parse("2026-04-22T12:00:00Z")));
		entityManager.persist(MeetingParticipant.join(joinedSuccess.getId(), 7L));

		Meeting joinedFailure = entityManager.persist(Meeting.create(
			1L, 21L, "joined failure", "Theme D", "Seoul", "2026-04-23", "13:00", 4, null, null, "desc"
		));
		joinedFailure.closeRecruitment();
		joinedFailure.complete();
		entityManager.persist(MeetingLog.create(joinedFailure.getId(), 7L, "failure", MeetingResult.FAILURE, Instant.parse("2026-04-23T12:00:00Z")));
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			joinedFailure.getId(),
			7L,
			MeetingParticipationStatus.APPROVED,
			null,
			null
		));

		Meeting recruiting = entityManager.persist(Meeting.create(
			1L, 7L, "recruiting", "Theme E", "Seoul", "2026-04-24", "14:00", 4, null, null, "desc"
		));
		entityManager.persist(MeetingParticipant.join(recruiting.getId(), 7L));

		entityManager.flush();
		entityManager.clear();

		var result = repository.findActivityRecordViewByUserId(7L);

		assertThat(result.completedCount()).isEqualTo(4L);
		assertThat(result.successCount()).isEqualTo(2L);
	}

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

	@Test
	void returnsCrewScheduleItemsWithParticipantCountInDateRange() {
		Crew crew = entityManager.persist(Crew.create("Schedule Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting recruiting = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Recruiting", "Theme A", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		Meeting completed = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Completed", "Theme B", "Gangnam", "2026-04-21", "19:00", 4, null, null, null
		));
		completed.closeRecruitment();
		completed.complete();
		entityManager.persistAndFlush(completed);
		Meeting canceled = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Canceled", "Theme C", "Seongsu", "2026-04-22", "20:00", 4, null, null, null
		));
		canceled.cancel();
		entityManager.persistAndFlush(canceled);
		entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Outside", "Theme D", "Jamsil", "2026-04-23", "21:00", 4, null, null, null
		));

		entityManager.persistAndFlush(MeetingParticipant.join(recruiting.getId(), 8L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, recruiting.getId(), 9L, MeetingParticipationStatus.APPROVED, null, null)
		);
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, recruiting.getId(), 10L, MeetingParticipationStatus.PENDING, null, null)
		);
		entityManager.persistAndFlush(MeetingParticipant.join(completed.getId(), 8L));
		entityManager.flush();
		entityManager.clear();

		CrewScheduleView result = repository.findCrewScheduleViewByCrewId(
			crew.getId(),
			LocalDate.parse("2026-04-20"),
			LocalDate.parse("2026-04-22")
		);

		assertThat(result.items()).extracting(CrewScheduleView.Item::meetingId)
			.containsExactly(recruiting.getId(), completed.getId(), canceled.getId());
		assertThat(result.items()).extracting(CrewScheduleView.Item::meetingStatus)
			.containsExactly(
				MeetingStatus.RECRUITING.name(),
				MeetingStatus.COMPLETED.name(),
				MeetingStatus.CANCELED.name()
			);
		assertThat(result.items()).extracting(CrewScheduleView.Item::recruitmentStatus)
			.containsExactly("OPEN", "CLOSED", "CLOSED");
		assertThat(result.items()).extracting(CrewScheduleView.Item::participantCount)
			.containsExactly(4L, 2L, 1L);
		assertThat(result.items()).extracting(CrewScheduleView.Item::isCanceled)
			.containsExactly(false, false, true);
	}

	@Test
	void returnsMeetingItemsWithParticipantCountIncludingHostAndJoinedParticipants() {
		Crew crew = entityManager.persist(Crew.create("Meeting Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting meeting = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Recruiting", "Theme A", "Hongdae", "2026-04-20", "18:00", 6, null, null, null
		));

		entityManager.persistAndFlush(MeetingParticipant.join(meeting.getId(), 8L));
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, meeting.getId(), 9L, MeetingParticipationStatus.APPROVED, null, null)
		);
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, meeting.getId(), 10L, MeetingParticipationStatus.PENDING, null, null)
		);
		entityManager.persistAndFlush(
			MeetingParticipant.rehydrate(null, meeting.getId(), 11L, MeetingParticipationStatus.LEFT, null, null)
		);
		entityManager.flush();
		entityManager.clear();

		MeetingsView result = repository.findMeetingsViewByCrewId(crew.getId(), 0, 20);

		assertThat(result.items()).extracting(MeetingsView.Item::meetingId)
			.containsExactly(meeting.getId());
		assertThat(result.items()).extracting(MeetingsView.Item::participantCount)
			.containsExactly(4L);
	}

	@Test
	void returnsRecruitingMeetingItemsBeforeClosedCompletedAndCanceledItems() {
		Crew crew = entityManager.persist(Crew.create("Meeting Sort Crew", "desc", CrewVisibility.PUBLIC, null));
		Meeting closed = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Closed", "Theme B", "Hongdae", "2026-04-20", "18:00", 4, null, null, null
		));
		closed.closeRecruitment();
		entityManager.persistAndFlush(closed);
		Meeting completed = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Completed", "Theme C", "Gangnam", "2026-04-21", "18:00", 4, null, null, null
		));
		completed.closeRecruitment();
		completed.complete();
		entityManager.persistAndFlush(completed);
		Meeting canceled = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Canceled", "Theme D", "Seongsu", "2026-04-22", "18:00", 4, null, null, null
		));
		canceled.cancel();
		entityManager.persistAndFlush(canceled);
		Meeting recruiting = entityManager.persistAndFlush(Meeting.create(
			crew.getId(), 7L, "Recruiting", "Theme A", "Jamsil", "2026-04-23", "18:00", 4, null, null, null
		));
		entityManager.flush();
		entityManager.clear();

		MeetingsView result = repository.findMeetingsViewByCrewId(crew.getId(), 0, 20);

		assertThat(result.items()).extracting(MeetingsView.Item::meetingId)
			.containsExactly(recruiting.getId(), closed.getId(), completed.getId(), canceled.getId());
	}
}
