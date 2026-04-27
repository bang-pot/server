package com.bangpot.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.domain.CrewJoinRequest;

class JpaCrewJoinRequestRepositoryTest {

	@Test
	void translatesPendingCrewUserUniqueViolationToAlreadyPendingException() {
		CrewJoinRequestJpaRepository crewJoinRequestJpaRepository = mock(CrewJoinRequestJpaRepository.class);
		CrewJoinRequest request = CrewJoinRequest.createPending(1L, 20L, "join");
		when(crewJoinRequestJpaRepository.saveAndFlush(request))
			.thenThrow(new DataIntegrityViolationException(
				"constraint violation",
				new ConstraintViolationException(
					"duplicate pending crew join request",
					new SQLException("duplicate key"),
					CrewJoinRequest.PENDING_CREW_USER_UNIQUE_CONSTRAINT
				)
			));
		CrewJoinRequestRepository crewJoinRequestRepository =
			new JpaCrewJoinRequestRepository(crewJoinRequestJpaRepository);

		assertThatThrownBy(() -> crewJoinRequestRepository.save(request))
			.isInstanceOf(CrewJoinRequestAlreadyPendingException.class);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintIsDifferent() {
		CrewJoinRequestJpaRepository crewJoinRequestJpaRepository = mock(CrewJoinRequestJpaRepository.class);
		CrewJoinRequest request = CrewJoinRequest.createPending(1L, 20L, "join");
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint violation",
			new ConstraintViolationException(
				"other constraint",
				new SQLException("not null"),
				"some_other_constraint"
			)
		);
		when(crewJoinRequestJpaRepository.saveAndFlush(request)).thenThrow(exception);
		CrewJoinRequestRepository crewJoinRequestRepository =
			new JpaCrewJoinRequestRepository(crewJoinRequestJpaRepository);

		assertThatThrownBy(() -> crewJoinRequestRepository.save(request))
			.isSameAs(exception);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintNameExistsOnlyInMessage() {
		CrewJoinRequestJpaRepository crewJoinRequestJpaRepository = mock(CrewJoinRequestJpaRepository.class);
		CrewJoinRequest request = CrewJoinRequest.createPending(1L, 20L, "join");
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint [uk_crew_join_requests_pending_crew_user]"
		);
		when(crewJoinRequestJpaRepository.saveAndFlush(request)).thenThrow(exception);
		CrewJoinRequestRepository crewJoinRequestRepository =
			new JpaCrewJoinRequestRepository(crewJoinRequestJpaRepository);

		assertThatThrownBy(() -> crewJoinRequestRepository.save(request))
			.isSameAs(exception);
	}
}
