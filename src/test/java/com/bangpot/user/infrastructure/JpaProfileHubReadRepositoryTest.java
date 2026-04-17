package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.user.application.port.ProfileHubReadRepository;

@DataJpaTest
class JpaProfileHubReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProfileHubReadRepository profileHubReadRepository;

	@Test
	void loadsProfileHubCountsWithCurrentRoundRules() {
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (7, 'bangpot', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (8, 'host-a', now(), now())")
			.executeUpdate();
		entityManager.getEntityManager()
			.createNativeQuery("insert into users (id, nickname, created_at, updated_at) values (9, 'host-b', now(), now())")
			.executeUpdate();

		Crew crewA = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew crewB = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew crewC = entityManager.persist(Crew.create("Gamma Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew privateCrew = entityManager.persist(Crew.create("Private Crew", "desc", CrewVisibility.PRIVATE, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persist(deletedCrew);

		entityManager.persist(CrewMember.createMember(crewA.getId(), 7L));
		entityManager.persist(CrewMember.createMember(crewB.getId(), 7L));
		entityManager.persist(CrewMember.createMember(deletedCrew.getId(), 7L));

		CrewJoinRequest pendingJoinRequest = CrewJoinRequest.createPending(crewC.getId(), 7L, "let me in");
		entityManager.persist(pendingJoinRequest);
		CrewJoinRequest privatePendingJoinRequest = CrewJoinRequest.createPending(privateCrew.getId(), 7L, "private pending");
		entityManager.persist(privatePendingJoinRequest);
		CrewJoinRequest deletedPendingJoinRequest = CrewJoinRequest.createPending(deletedCrew.getId(), 7L, "deleted pending");
		entityManager.persist(deletedPendingJoinRequest);
		CrewJoinRequest approvedJoinRequest = CrewJoinRequest.createPending(crewB.getId(), 7L, "approved already");
		approvedJoinRequest.approve();
		entityManager.persist(approvedJoinRequest);

		entityManager.persist(Meeting.create(
			crewA.getId(), 7L, "내가 만든 모집중 모임", "Theme A", "홍대", "2026-04-20", "10:00", 4, null, null, "desc"
		));
		Meeting createdCanceled = entityManager.persist(Meeting.create(
			crewA.getId(), 7L, "내가 만든 취소 모임", "Theme B", "강남", "2026-04-21", "11:00", 4, null, null, "desc"
		));
		createdCanceled.cancel();

		Meeting joinedMeeting = entityManager.persist(Meeting.create(
			crewA.getId(), 8L, "참여한 타인 모임", "Theme C", "건대", "2026-04-22", "12:00", 4, null, null, "desc"
		));
		Meeting leftMeeting = entityManager.persist(Meeting.create(
			crewA.getId(), 9L, "나간 타인 모임", "Theme D", "신촌", "2026-04-23", "13:00", 4, null, null, "desc"
		));
		Meeting pendingMeeting = entityManager.persist(Meeting.create(
			crewB.getId(), 8L, "레거시 pending 모임", "Theme E", "잠실", "2026-04-24", "14:00", 4, null, null, "desc"
		));
		Meeting approvedMeeting = entityManager.persist(Meeting.create(
			crewB.getId(), 8L, "레거시 approved 모임", "Theme F", "성수", "2026-04-25", "15:00", 4, null, null, "desc"
		));
		Meeting hostedParticipationMeeting = entityManager.persist(Meeting.create(
			crewB.getId(), 7L, "호스트인 내 모임", "Theme G", "합정", "2026-04-26", "16:00", 4, null, null, "desc"
		));

		entityManager.persist(MeetingParticipant.join(joinedMeeting.getId(), 7L));
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			leftMeeting.getId(),
			7L,
			MeetingParticipationStatus.LEFT,
			null,
			null
		));
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			pendingMeeting.getId(),
			7L,
			MeetingParticipationStatus.PENDING,
			null,
			null
		));
		entityManager.persist(MeetingParticipant.rehydrate(
			null,
			approvedMeeting.getId(),
			7L,
			MeetingParticipationStatus.APPROVED,
			null,
			null
		));
		entityManager.persist(MeetingParticipant.join(hostedParticipationMeeting.getId(), 7L));

		entityManager.flush();
		entityManager.clear();

		ProfileHubReadRepository.Counts counts = profileHubReadRepository.loadCounts(7L);

		assertThat(counts.createdMeetingsCount()).isEqualTo(3L);
		assertThat(counts.joinedMeetingsCount()).isEqualTo(3L);
		assertThat(counts.myCrewsCount()).isEqualTo(2L);
		assertThat(counts.pendingCrewsCount()).isEqualTo(1L);
	}
}
