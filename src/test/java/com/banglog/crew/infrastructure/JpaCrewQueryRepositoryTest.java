package com.banglog.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.ExploreCrewSort;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.crew.domain.view.ExploreCrewCardsView;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingParticipationStatus;

@DataJpaTest
@Import(JpaCrewQueryRepository.class)
class JpaCrewQueryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CrewQueryRepository repository;

	@Test
	void returnsOnlyActivePublicCrewsWithActiveMemberCountOrderedByNewestFirst() {
		insertUser(1L, "leader");
		insertUser(2L, "member-a");
		insertUser(3L, "member-b");

		Crew olderPublic = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew newerPublic = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, "https://cdn.example.com/beta.jpg"));
		Crew privateCrew = entityManager.persist(Crew.create("Private Crew", "desc", CrewVisibility.PRIVATE, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		entityManager.persistAndFlush(CrewMember.createLeader(olderPublic.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(newerPublic.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(privateCrew.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(deletedCrew.getId(), 1L));

		entityManager.persistAndFlush(CrewMember.createMember(olderPublic.getId(), 2L));
		entityManager.persistAndFlush(CrewMember.createMember(newerPublic.getId(), 2L));
		CrewMember leftMember = CrewMember.createMember(newerPublic.getId(), 3L);
		leftMember.leave();
		entityManager.persistAndFlush(leftMember);
		entityManager.persistAndFlush(CrewMember.createMember(privateCrew.getId(), 2L));

		entityManager.clear();

		var result = repository.findPublicCrewPreviewView(8);

		assertThat(result.items()).extracting(com.banglog.crew.domain.view.PublicCrewPreviewView.Item::crewId)
			.containsExactly(newerPublic.getId(), olderPublic.getId());
		assertThat(result.items()).extracting(com.banglog.crew.domain.view.PublicCrewPreviewView.Item::crewName)
			.containsExactly("Beta Crew", "Alpha Crew");
		assertThat(result.items()).extracting(com.banglog.crew.domain.view.PublicCrewPreviewView.Item::memberCount)
			.containsExactly(2L, 2L);
		assertThat(result.items()).extracting(com.banglog.crew.domain.view.PublicCrewPreviewView.Item::coverImageUrl)
			.containsExactly("https://cdn.example.com/beta.jpg", null);
	}

	@Test
	void limitsPublicCrewPreviewToRequestedSize() {
		insertUser(1L, "leader");

		Crew first = entityManager.persist(Crew.create("First", "desc", CrewVisibility.PUBLIC, null));
		Crew second = entityManager.persist(Crew.create("Second", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(first.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(second.getId(), 1L));

		entityManager.clear();

		var result = repository.findPublicCrewPreviewView(1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).crewId()).isEqualTo(second.getId());
	}

	@Test
	void returnsExploreCrewCardsForActivePublicAndPrivateCrews() {
		insertUser(10L, "public-leader");
		insertUser(11L, "private-leader");
		insertUser(12L, "deleted-leader");
		insertUser(20L, "active-member");
		insertUser(21L, "left-member");
		insertUser(22L, "removed-member");

		Crew publicCrew = persistCrew("Public Crew", "public desc", CrewVisibility.PUBLIC, "2026-01-01T00:00:00Z");
		Crew privateCrew = persistCrew("Private Crew", "private desc", CrewVisibility.PRIVATE, "2026-01-02T00:00:00Z");
		Crew deletedCrew = persistCrew("Deleted Crew", "deleted desc", CrewVisibility.PUBLIC, "2026-01-03T00:00:00Z");
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		entityManager.persistAndFlush(CrewMember.createLeader(publicCrew.getId(), 10L));
		entityManager.persistAndFlush(CrewMember.createLeader(privateCrew.getId(), 11L));
		entityManager.persistAndFlush(CrewMember.createLeader(deletedCrew.getId(), 12L));
		entityManager.persistAndFlush(CrewMember.createMember(privateCrew.getId(), 20L));
		CrewMember leftMember = CrewMember.createMember(privateCrew.getId(), 21L);
		leftMember.leave();
		entityManager.persistAndFlush(leftMember);
		CrewMember removedMember = CrewMember.createMember(privateCrew.getId(), 22L);
		removedMember.remove();
		entityManager.persistAndFlush(removedMember);

		entityManager.clear();

		ExploreCrewCardsView result = repository.findExploreCrewCardsView(null, ExploreCrewSort.LATEST, 0, 20);

		assertThat(result.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(privateCrew.getId(), publicCrew.getId());
		assertThat(result.items()).extracting(ExploreCrewCardsView.Item::visibility)
			.containsExactly(CrewVisibility.PRIVATE, CrewVisibility.PUBLIC);
		assertThat(result.items()).extracting(ExploreCrewCardsView.Item::leaderNickname)
			.containsExactly("private-leader", "public-leader");
		assertThat(result.items()).extracting(ExploreCrewCardsView.Item::memberCount)
			.containsExactly(2L, 1L);
	}

	@Test
	void searchesExploreCrewCardsByCrewNameAndLeaderNicknameAndTreatsBlankKeywordAsDefault() {
		insertUser(30L, "escape-boss");
		insertUser(31L, "captain");

		Crew mysteryCrew = persistCrew("Mystery Room", "desc", CrewVisibility.PRIVATE, "2026-01-01T00:00:00Z");
		Crew puzzleCrew = persistCrew("Puzzle Crew", "desc", CrewVisibility.PUBLIC, "2026-01-02T00:00:00Z");
		entityManager.persistAndFlush(CrewMember.createLeader(mysteryCrew.getId(), 30L));
		entityManager.persistAndFlush(CrewMember.createLeader(puzzleCrew.getId(), 31L));

		entityManager.clear();

		ExploreCrewCardsView crewNameResult = repository.findExploreCrewCardsView(
			"Mystery",
			ExploreCrewSort.LATEST,
			0,
			20
		);
		ExploreCrewCardsView leaderNicknameResult = repository.findExploreCrewCardsView(
			"escape",
			ExploreCrewSort.LATEST,
			0,
			20
		);
		ExploreCrewCardsView blankKeywordResult = repository.findExploreCrewCardsView(null, ExploreCrewSort.LATEST, 0, 20);

		assertThat(crewNameResult.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(mysteryCrew.getId());
		assertThat(leaderNicknameResult.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(mysteryCrew.getId());
		assertThat(blankKeywordResult.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(puzzleCrew.getId(), mysteryCrew.getId());
	}

	@Test
	void sortsExploreCrewCardsByCreatedAtAndActiveMemberCountWithStableTieBreakers() {
		insertUser(40L, "leader-a");
		insertUser(41L, "leader-b");
		insertUser(42L, "leader-c");
		insertUser(43L, "leader-d");
		insertUser(50L, "member-a");
		insertUser(51L, "member-b");
		insertUser(52L, "member-c");
		insertUser(53L, "member-d");

		Crew alpha = persistCrew("Alpha", "desc", CrewVisibility.PUBLIC, "2026-01-01T00:00:00Z");
		Crew beta = persistCrew("Beta", "desc", CrewVisibility.PUBLIC, "2026-01-02T00:00:00Z");
		Crew gamma = persistCrew("Gamma", "desc", CrewVisibility.PUBLIC, "2026-01-03T00:00:00Z");
		Crew delta = persistCrew("Delta", "desc", CrewVisibility.PUBLIC, "2026-01-03T00:00:00Z");

		entityManager.persistAndFlush(CrewMember.createLeader(alpha.getId(), 40L));
		entityManager.persistAndFlush(CrewMember.createLeader(beta.getId(), 41L));
		entityManager.persistAndFlush(CrewMember.createLeader(gamma.getId(), 42L));
		entityManager.persistAndFlush(CrewMember.createLeader(delta.getId(), 43L));
		entityManager.persistAndFlush(CrewMember.createMember(beta.getId(), 50L));
		entityManager.persistAndFlush(CrewMember.createMember(beta.getId(), 51L));
		entityManager.persistAndFlush(CrewMember.createMember(gamma.getId(), 52L));
		entityManager.persistAndFlush(CrewMember.createMember(delta.getId(), 53L));

		entityManager.clear();

		ExploreCrewCardsView latest = repository.findExploreCrewCardsView(null, ExploreCrewSort.LATEST, 0, 20);
		ExploreCrewCardsView oldest = repository.findExploreCrewCardsView(null, ExploreCrewSort.OLDEST, 0, 20);
		ExploreCrewCardsView memberCountDesc = repository.findExploreCrewCardsView(
			null,
			ExploreCrewSort.MEMBER_COUNT_DESC,
			0,
			20
		);
		ExploreCrewCardsView memberCountAsc = repository.findExploreCrewCardsView(
			null,
			ExploreCrewSort.MEMBER_COUNT_ASC,
			0,
			20
		);

		assertThat(latest.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(delta.getId(), gamma.getId(), beta.getId(), alpha.getId());
		assertThat(oldest.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(alpha.getId(), beta.getId(), gamma.getId(), delta.getId());
		assertThat(memberCountDesc.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(beta.getId(), delta.getId(), gamma.getId(), alpha.getId());
		assertThat(memberCountAsc.items()).extracting(ExploreCrewCardsView.Item::crewId)
			.containsExactly(alpha.getId(), delta.getId(), gamma.getId(), beta.getId());
	}

	@Test
	void returnsMeetingCreateCrewsForActiveMembershipsOnly() {
		insertUser(1L, "member");
		insertUser(2L, "left-member");

		Crew alpha = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew beta = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		entityManager.persistAndFlush(CrewMember.createMember(beta.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(alpha.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createMember(deletedCrew.getId(), 1L));
		CrewMember leftMember = CrewMember.createMember(alpha.getId(), 2L);
		leftMember.leave();
		entityManager.persistAndFlush(leftMember);

		entityManager.clear();

		MeetingCreateCrewsView result = repository.findActiveCrewsByUserId(1L);

		assertThat(result.items()).extracting(MeetingCreateCrewsView.Item::crewName)
			.containsExactly("Alpha Crew", "Beta Crew");
	}

	@Test
	void returnsCrewMembersViewForJoinedMember() {
		insertUser(1L, "leader");
		insertUser(2L, "member");

		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(crew.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createMember(crew.getId(), 2L));

		entityManager.clear();

		CrewMembersView result = repository.findCrewMembersViewByCrewIdAndUserId(crew.getId(), 2L, 0, 20).orElseThrow();

		assertThat(result.myRole()).isEqualTo(com.banglog.crew.domain.CrewRole.MEMBER);
		assertThat(result.items()).extracting(CrewMembersView.Item::userId)
			.containsExactly(1L, 2L);
		assertThat(result.items()).extracting(CrewMembersView.Item::nickname)
			.containsExactly("leader", "member");
		assertThat(result.page()).isEqualTo(CrewMembersView.Page.of(0, 20, false));
	}

	@Test
	void returnsCrewMembersViewByPageWithLeaderFirst() {
		insertUser(1L, "leader");
		for (long userId = 2L; userId <= 23L; userId++) {
			insertUser(userId, "member-" + userId);
		}

		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(crew.getId(), 1L));
		for (long userId = 2L; userId <= 23L; userId++) {
			entityManager.persistAndFlush(CrewMember.createMember(crew.getId(), userId));
		}

		entityManager.clear();

		CrewMembersView firstPage = repository.findCrewMembersViewByCrewIdAndUserId(crew.getId(), 2L, 0, 20).orElseThrow();
		CrewMembersView secondPage = repository.findCrewMembersViewByCrewIdAndUserId(crew.getId(), 2L, 1, 20).orElseThrow();

		assertThat(firstPage.items()).hasSize(20);
		assertThat(firstPage.items().get(0).role()).isEqualTo(com.banglog.crew.domain.CrewRole.LEADER);
		assertThat(firstPage.page()).isEqualTo(CrewMembersView.Page.of(0, 20, true));
		assertThat(secondPage.items()).hasSize(3);
		assertThat(secondPage.items()).extracting(CrewMembersView.Item::role)
			.containsOnly(com.banglog.crew.domain.CrewRole.MEMBER);
		assertThat(secondPage.page()).isEqualTo(CrewMembersView.Page.of(1, 20, false));
	}

	@Test
	void returnsCrewMembersViewWithCompletedMeetingEscapeCount() {
		insertUser(1L, "leader");
		insertUser(2L, "member");
		insertUser(3L, "other-member");

		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew otherCrew = entityManager.persist(Crew.create("Other Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(crew.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createMember(crew.getId(), 2L));
		entityManager.persistAndFlush(CrewMember.createMember(crew.getId(), 3L));
		entityManager.persistAndFlush(CrewMember.createLeader(otherCrew.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createMember(otherCrew.getId(), 2L));

		persistCompletedMeeting(crew.getId(), 2L, "hosted");
		Meeting joinedCompleted = persistCompletedMeeting(crew.getId(), 1L, "joined");
		entityManager.persistAndFlush(MeetingParticipant.join(joinedCompleted.getId(), 2L));
		Meeting leftCompleted = persistCompletedMeeting(crew.getId(), 1L, "left");
		entityManager.persistAndFlush(MeetingParticipant.rehydrate(
			null,
			leftCompleted.getId(),
			2L,
			MeetingParticipationStatus.LEFT,
			null,
			null
		));
		Meeting recruiting = persistRecruitingMeeting(crew.getId(), 1L, "recruiting");
		entityManager.persistAndFlush(MeetingParticipant.join(recruiting.getId(), 2L));
		persistCompletedMeeting(otherCrew.getId(), 2L, "other-crew");

		entityManager.clear();

		CrewMembersView result = repository.findCrewMembersViewByCrewIdAndUserId(crew.getId(), 2L, 0, 20).orElseThrow();

		assertThat(result.items())
			.filteredOn(item -> item.userId().equals(2L))
			.singleElement()
			.extracting(CrewMembersView.Item::escapeCount)
			.isEqualTo(2);
	}

	private void insertUser(Long userId, String nickname) {
		entityManager.getEntityManager().createNativeQuery("""
			insert into users (id, nickname, bio, gender, profile_image_url, created_at, updated_at, withdrawn_at)
			values (?, ?, null, null, null, now(), now(), null)
			""")
			.setParameter(1, userId)
			.setParameter(2, nickname)
			.executeUpdate();
	}

	private Crew persistCrew(String name, String description, CrewVisibility visibility, String createdAt) {
		Crew crew = entityManager.persistAndFlush(Crew.create(name, description, visibility, null));
		entityManager.getEntityManager().createNativeQuery("""
			update crews
			set created_at = ?, updated_at = ?
			where id = ?
			""")
			.setParameter(1, Timestamp.from(Instant.parse(createdAt)))
			.setParameter(2, Timestamp.from(Instant.parse(createdAt)))
			.setParameter(3, crew.getId())
			.executeUpdate();
		return crew;
	}

	private Meeting persistCompletedMeeting(Long crewId, Long hostUserId, String suffix) {
		Meeting meeting = persistRecruitingMeeting(crewId, hostUserId, suffix);
		meeting.closeRecruitment();
		meeting.complete();
		return entityManager.persistAndFlush(meeting);
	}

	private Meeting persistRecruitingMeeting(Long crewId, Long hostUserId, String suffix) {
		return entityManager.persistAndFlush(Meeting.create(
			crewId,
			hostUserId,
			"모임 " + suffix,
			"테마 " + suffix,
			"장소",
			"2026-05-20",
			"10:00",
			4,
			null,
			null,
			"설명"
		));
	}
}
