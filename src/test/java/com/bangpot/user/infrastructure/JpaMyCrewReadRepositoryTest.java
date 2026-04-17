package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.MyCrewReadRepository;

@DataJpaTest
class JpaMyCrewReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private MyCrewReadRepository repository;

	@Test
	void returnsOnlyActiveMembershipsOnActiveCrewsAndSortsByName() {
		UserJpaEntity leaderA = entityManager.persist(UserJpaEntity.create(1L, "leader-a"));
		UserJpaEntity leaderB = entityManager.persist(UserJpaEntity.create(2L, "leader-b"));
		UserJpaEntity me = entityManager.persist(UserJpaEntity.create(7L, "bangpot"));
		entityManager.flush();

		Crew alphaCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew betaCrew = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PRIVATE, "https://cdn.example.com/beta.jpg"));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		entityManager.persistAndFlush(CrewMember.createLeader(alphaCrew.getId(), leaderA.getId()));
		entityManager.persistAndFlush(CrewMember.createLeader(betaCrew.getId(), leaderB.getId()));
		entityManager.persistAndFlush(CrewMember.createLeader(deletedCrew.getId(), leaderA.getId()));

		entityManager.persistAndFlush(CrewMember.createMember(betaCrew.getId(), me.getId()));
		entityManager.persistAndFlush(CrewMember.createMember(alphaCrew.getId(), me.getId()));

		CrewMember leftMember = CrewMember.createMember(deletedCrew.getId(), me.getId());
		leftMember.leave();
		entityManager.persistAndFlush(leftMember);

		Crew removedCrew = entityManager.persist(Crew.create("Removed Crew", "desc", CrewVisibility.PUBLIC, null));
		entityManager.persistAndFlush(removedCrew);
		entityManager.persistAndFlush(CrewMember.createLeader(removedCrew.getId(), leaderA.getId()));
		CrewMember removedMember = CrewMember.createMember(removedCrew.getId(), me.getId());
		removedMember.remove();
		entityManager.persistAndFlush(removedMember);

		entityManager.clear();

		MyCrewReadRepository.SearchResult result = repository.search(7L, 0, 10);

		assertThat(result.items()).extracting(MyCrewReadRepository.Item::crewId)
			.containsExactly(alphaCrew.getId(), betaCrew.getId());
		assertThat(result.items()).extracting(MyCrewReadRepository.Item::crewName)
			.containsExactly("Alpha Crew", "Beta Crew");
		assertThat(result.items()).extracting(MyCrewReadRepository.Item::visibility)
			.containsExactly("PUBLIC", "PRIVATE");
		assertThat(result.items()).extracting(MyCrewReadRepository.Item::leaderNickname)
			.containsExactly("leader-a", "leader-b");
		assertThat(result.items()).extracting(MyCrewReadRepository.Item::coverImageUrl)
			.containsExactly(null, "https://cdn.example.com/beta.jpg");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMoreCrewsExistThanRequestedSize() {
		UserJpaEntity leaderA = entityManager.persist(UserJpaEntity.create(1L, "leader-a"));
		UserJpaEntity leaderB = entityManager.persist(UserJpaEntity.create(2L, "leader-b"));
		UserJpaEntity me = entityManager.persist(UserJpaEntity.create(7L, "bangpot"));
		entityManager.flush();

		Crew alphaCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew betaCrew = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));

		entityManager.persistAndFlush(CrewMember.createLeader(alphaCrew.getId(), leaderA.getId()));
		entityManager.persistAndFlush(CrewMember.createLeader(betaCrew.getId(), leaderB.getId()));
		entityManager.persistAndFlush(CrewMember.createMember(alphaCrew.getId(), me.getId()));
		entityManager.persistAndFlush(CrewMember.createMember(betaCrew.getId(), me.getId()));

		entityManager.clear();

		MyCrewReadRepository.SearchResult result = repository.search(7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}
}
