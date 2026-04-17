package com.bangpot.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.PendingCrewReadRepository;

@DataJpaTest
class JpaPendingCrewReadRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PendingCrewReadRepository repository;

	@Test
	void returnsOnlyPendingRequestsForCurrentUserOnActivePublicCrewsAndSortsByLatestRequest() {
		Crew publicCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew publicCrewNoMessage = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew privateCrew = entityManager.persist(Crew.create("Gamma Crew", "desc", CrewVisibility.PRIVATE, null));
		Crew deletedCrew = entityManager.persist(Crew.create("Deleted Crew", "desc", CrewVisibility.PUBLIC, null));
		deletedCrew.delete();
		entityManager.persistAndFlush(deletedCrew);

		CrewJoinRequest older = entityManager.persistAndFlush(
			CrewJoinRequest.createPending(publicCrew.getId(), 7L, "  함께 활동하고 싶습니다 \n 잘 부탁드립니다  ")
		);
		CrewJoinRequest latest = entityManager.persistAndFlush(
			CrewJoinRequest.createPending(publicCrewNoMessage.getId(), 7L, "   ")
		);

		entityManager.persistAndFlush(CrewJoinRequest.createPending(privateCrew.getId(), 7L, "private"));
		entityManager.persistAndFlush(CrewJoinRequest.createPending(deletedCrew.getId(), 7L, "deleted"));

		CrewJoinRequest approved = CrewJoinRequest.createPending(publicCrew.getId(), 8L, "approved");
		approved.approve();
		entityManager.persistAndFlush(approved);

		entityManager.clear();

		PendingCrewReadRepository.SearchResult result = repository.search(7L, 0, 10);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items()).extracting(PendingCrewReadRepository.Item::joinRequestId)
			.containsExactly(latest.getId(), older.getId());
		assertThat(result.items()).extracting(PendingCrewReadRepository.Item::crewId)
			.containsExactly(publicCrewNoMessage.getId(), publicCrew.getId());
		assertThat(result.items()).extracting(PendingCrewReadRepository.Item::crewName)
			.containsExactly("Beta Crew", "Alpha Crew");
		assertThat(result.items()).extracting(PendingCrewReadRepository.Item::messageSummary)
			.containsExactly(null, "함께 활동하고 싶습니다 잘 부탁드립니다");
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void returnsHasNextWhenMorePendingRequestsExistThanRequestedSize() {
		Crew firstCrew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		Crew secondCrew = entityManager.persist(Crew.create("Beta Crew", "desc", CrewVisibility.PUBLIC, null));

		entityManager.persistAndFlush(CrewJoinRequest.createPending(firstCrew.getId(), 7L, "first"));
		entityManager.persistAndFlush(CrewJoinRequest.createPending(secondCrew.getId(), 7L, "second"));
		entityManager.clear();

		PendingCrewReadRepository.SearchResult result = repository.search(7L, 0, 1);

		assertThat(result.items()).hasSize(1);
		assertThat(result.pageInfo().hasNext()).isTrue();
	}

	@Test
	void cancelsOnlyMyPendingRequest() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		CrewJoinRequest mine = entityManager.persistAndFlush(CrewJoinRequest.createPending(crew.getId(), 7L, "mine"));
		entityManager.persistAndFlush(CrewJoinRequest.createPending(crew.getId(), 8L, "other"));
		entityManager.clear();

		PendingCrewReadRepository.CancelResult result = repository.cancel(7L, mine.getId());

		assertThat(result.joinRequestId()).isEqualTo(mine.getId());
		assertThat(result.crewId()).isEqualTo(crew.getId());
	}

	@Test
	void rejectsCancelWhenRequestIsNotOwnedByCurrentUser() {
		Crew crew = entityManager.persist(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		CrewJoinRequest otherUsersRequest = entityManager.persistAndFlush(
			CrewJoinRequest.createPending(crew.getId(), 8L, "other")
		);
		entityManager.clear();

		assertThatThrownBy(() -> repository.cancel(7L, otherUsersRequest.getId()))
			.isInstanceOf(CrewJoinRequestNotFoundException.class);
	}
}
