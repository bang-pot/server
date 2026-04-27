package com.bangpot.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;

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

		assertThat(result.items()).extracting(com.bangpot.crew.domain.view.PublicCrewPreviewView.Item::crewId)
			.containsExactly(newerPublic.getId(), olderPublic.getId());
		assertThat(result.items()).extracting(com.bangpot.crew.domain.view.PublicCrewPreviewView.Item::crewName)
			.containsExactly("Beta Crew", "Alpha Crew");
		assertThat(result.items()).extracting(com.bangpot.crew.domain.view.PublicCrewPreviewView.Item::memberCount)
			.containsExactly(2L, 2L);
		assertThat(result.items()).extracting(com.bangpot.crew.domain.view.PublicCrewPreviewView.Item::coverImageUrl)
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
	void returnsPublicCrewCardsSliceForPublicCrewExplore() {
		Crew first = entityManager.persist(Crew.create("First", "first desc", CrewVisibility.PUBLIC, null));
		Crew second = entityManager.persist(Crew.create("Second", "second desc", CrewVisibility.PUBLIC, "https://cdn.example.com/second.jpg"));
		entityManager.persist(Crew.create("Private", "private desc", CrewVisibility.PRIVATE, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted", "deleted desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		entityManager.clear();

		var firstPage = repository.findPublicCrewCardsView(0, 1);
		var secondPage = repository.findPublicCrewCardsView(1, 1);

		assertThat(firstPage.items()).extracting(com.bangpot.crew.domain.view.PublicCrewCardsView.Item::crewId)
			.containsExactly(first.getId());
		assertThat(firstPage.page().hasNext()).isTrue();
		assertThat(secondPage.items()).extracting(com.bangpot.crew.domain.view.PublicCrewCardsView.Item::crewId)
			.containsExactly(second.getId());
		assertThat(secondPage.items().get(0).imageUrl()).isEqualTo("https://cdn.example.com/second.jpg");
		assertThat(secondPage.page().hasNext()).isFalse();
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

		CrewMembersView result = repository.findCrewMembersViewByCrewIdAndUserId(crew.getId(), 2L).orElseThrow();

		assertThat(result.myRole()).isEqualTo(com.bangpot.crew.domain.CrewRole.MEMBER);
		assertThat(result.items()).extracting(CrewMembersView.Item::userId)
			.containsExactly(1L, 2L);
		assertThat(result.items()).extracting(CrewMembersView.Item::nickname)
			.containsExactly("leader", "member");
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
}
