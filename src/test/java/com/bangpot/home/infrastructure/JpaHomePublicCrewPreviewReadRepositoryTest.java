package com.bangpot.home.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.home.application.port.HomePublicCrewPreviewReadRepository;

@DataJpaTest
class JpaHomePublicCrewPreviewReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private HomePublicCrewPreviewReadRepository repository;

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

		var result = repository.findPreviewItems(8);

		assertThat(result).extracting(HomePublicCrewPreviewReadRepository.Item::crewId)
			.containsExactly(newerPublic.getId(), olderPublic.getId());
		assertThat(result).extracting(HomePublicCrewPreviewReadRepository.Item::crewName)
			.containsExactly("Beta Crew", "Alpha Crew");
		assertThat(result).extracting(HomePublicCrewPreviewReadRepository.Item::memberCount)
			.containsExactly(2L, 2L);
		assertThat(result).extracting(HomePublicCrewPreviewReadRepository.Item::coverImageUrl)
			.containsExactly("https://cdn.example.com/beta.jpg", null);
		assertThat(result).allMatch(HomePublicCrewPreviewReadRepository.Item::isPublic);
	}

	@Test
	void limitsPublicCrewPreviewToRequestedSize() {
		insertUser(1L, "leader");

		Crew first = entityManager.persist(Crew.create("First", "desc", CrewVisibility.PUBLIC, null));
		Crew second = entityManager.persist(Crew.create("Second", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(CrewMember.createLeader(first.getId(), 1L));
		entityManager.persistAndFlush(CrewMember.createLeader(second.getId(), 1L));

		entityManager.clear();

		var result = repository.findPreviewItems(1);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).crewId()).isEqualTo(second.getId());
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
