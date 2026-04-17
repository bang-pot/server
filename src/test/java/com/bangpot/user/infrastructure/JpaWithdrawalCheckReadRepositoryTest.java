package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.user.application.port.WithdrawalCheckReadRepository;

@DataJpaTest
class JpaWithdrawalCheckReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private WithdrawalCheckReadRepository repository;

	@Test
	void returnsOnlyActiveCrewsAndUnfinishedHostedOrJoinedMeetingsOnActiveCrews() {
		entityManager.persistAndFlush(UserJpaEntity.create(7L, "withdraw-user"));

		Crew activeLeaderCrew = entityManager.persistAndFlush(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(activeLeaderCrew.getId(), 7L));

		Crew activeMemberCrew = entityManager.persistAndFlush(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createMember(activeMemberCrew.getId(), 7L));

		Crew leftCrew = entityManager.persistAndFlush(Crew.create("Left Crew", "desc", CrewVisibility.PUBLIC, null));
		CrewMember leftMember = entityManager.persistAndFlush(CrewMember.createMember(leftCrew.getId(), 7L));
		leftMember.leave();
		entityManager.persistAndFlush(leftMember);

		Crew deletedCrew = entityManager.persistAndFlush(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createMember(deletedCrew.getId(), 7L));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		Meeting hostedRecruitingMeeting = entityManager.persistAndFlush(
			Meeting.create(activeLeaderCrew.getId(), 7L, "Hosted Recruiting", "Deep Blue", "Hongdae", "2026-04-20", "19:00", 4, null, null, "desc")
		);

		Meeting hostedClosedMeeting = entityManager.persistAndFlush(
			Meeting.create(activeLeaderCrew.getId(), 7L, "Hosted Closed", "Red Door", "Gangnam", "2026-04-21", "20:00", 4, null, null, "desc")
		);
		hostedClosedMeeting.closeRecruitment();
		entityManager.persistAndFlush(hostedClosedMeeting);

		Meeting joinedRecruitingMeeting = entityManager.persistAndFlush(
			Meeting.create(activeMemberCrew.getId(), 70L, "Joined Recruiting", "Orange", "Seongsu", "2026-04-22", "18:00", 4, null, null, "desc")
		);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			joinedRecruitingMeeting.getId(),
			7L,
			MeetingParticipationStatus.JOINED,
			Instant.parse("2026-04-10T00:00:00Z"),
			Instant.parse("2026-04-10T00:00:00Z")
		));

		Meeting approvedClosedMeeting = entityManager.persistAndFlush(
			Meeting.create(activeMemberCrew.getId(), 71L, "Approved Closed", "Purple", "Jamsil", "2026-04-23", "21:00", 4, null, null, "desc")
		);
		approvedClosedMeeting.closeRecruitment();
		entityManager.persistAndFlush(approvedClosedMeeting);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			approvedClosedMeeting.getId(),
			7L,
			MeetingParticipationStatus.APPROVED,
			Instant.parse("2026-04-11T00:00:00Z"),
			Instant.parse("2026-04-11T00:00:00Z")
		));

		Meeting pendingMeeting = entityManager.persistAndFlush(
			Meeting.create(activeMemberCrew.getId(), 72L, "Pending Meeting", "Sky", "Mapo", "2026-04-24", "18:30", 4, null, null, "desc")
		);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			pendingMeeting.getId(),
			7L,
			MeetingParticipationStatus.PENDING,
			Instant.parse("2026-04-12T00:00:00Z"),
			Instant.parse("2026-04-12T00:00:00Z")
		));

		Meeting leftMeeting = entityManager.persistAndFlush(
			Meeting.create(activeMemberCrew.getId(), 73L, "Left Meeting", "Gray", "Hapjeong", "2026-04-25", "17:00", 4, null, null, "desc")
		);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			leftMeeting.getId(),
			7L,
			MeetingParticipationStatus.LEFT,
			Instant.parse("2026-04-13T00:00:00Z"),
			Instant.parse("2026-04-13T00:00:00Z")
		));

		Meeting completedMeeting = entityManager.persistAndFlush(
			Meeting.create(activeMemberCrew.getId(), 74L, "Completed Meeting", "Black", "Yeonnam", "2026-04-10", "18:00", 4, null, null, "desc")
		);
		completedMeeting.closeRecruitment();
		completedMeeting.complete();
		entityManager.persistAndFlush(completedMeeting);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			completedMeeting.getId(),
			7L,
			MeetingParticipationStatus.JOINED,
			Instant.parse("2026-04-09T00:00:00Z"),
			Instant.parse("2026-04-09T00:00:00Z")
		));

		Meeting deletedCrewMeeting = entityManager.persistAndFlush(
			Meeting.create(deletedCrew.getId(), 75L, "Deleted Crew Meeting", "Mint", "Jamsil", "2026-04-26", "19:30", 4, null, null, "desc")
		);
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			deletedCrewMeeting.getId(),
			7L,
			MeetingParticipationStatus.JOINED,
			Instant.parse("2026-04-14T00:00:00Z"),
			Instant.parse("2026-04-14T00:00:00Z")
		));

		entityManager.clear();

		WithdrawalCheckReadRepository.View result = repository.load(7L);

		assertThat(result.blockingActiveCrews())
			.extracting(WithdrawalCheckReadRepository.ActiveCrew::crewId)
			.containsExactly(activeLeaderCrew.getId(), activeMemberCrew.getId());

		assertThat(result.blockingParticipatingMeetings())
			.extracting(WithdrawalCheckReadRepository.ParticipatingMeeting::meetingId)
			.containsExactly(
				hostedRecruitingMeeting.getId(),
				hostedClosedMeeting.getId(),
				joinedRecruitingMeeting.getId(),
				approvedClosedMeeting.getId()
			);

		assertThat(result.blockingParticipatingMeetings())
			.extracting(WithdrawalCheckReadRepository.ParticipatingMeeting::participationRole)
			.containsExactly("HOST", "HOST", "PARTICIPANT", "PARTICIPANT");
	}
}
