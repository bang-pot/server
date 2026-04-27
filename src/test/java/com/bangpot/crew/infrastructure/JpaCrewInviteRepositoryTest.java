package com.bangpot.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.bangpot.crew.application.exception.CrewInviteAlreadyPendingException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.domain.CrewInvite;

class JpaCrewInviteRepositoryTest {

	@Test
	void translatesCrewTargetUniqueViolationToAlreadyPendingException() {
		CrewInviteJpaRepository crewInviteJpaRepository = mock(CrewInviteJpaRepository.class);
		CrewInvite invite = CrewInvite.createPending(1L, 10L, 20L);
		when(crewInviteJpaRepository.saveAndFlush(invite))
			.thenThrow(new DataIntegrityViolationException(
				"constraint violation",
				new ConstraintViolationException(
					"duplicate crew invite",
					new SQLException("duplicate key"),
					CrewInvite.PENDING_CREW_TARGET_UNIQUE_CONSTRAINT
				)
			));
		CrewInviteRepository crewInviteRepository = new JpaCrewInviteRepository(crewInviteJpaRepository);

		assertThatThrownBy(() -> crewInviteRepository.save(invite))
			.isInstanceOf(CrewInviteAlreadyPendingException.class);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintIsDifferent() {
		CrewInviteJpaRepository crewInviteJpaRepository = mock(CrewInviteJpaRepository.class);
		CrewInvite invite = CrewInvite.createPending(1L, 10L, 20L);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint violation",
			new ConstraintViolationException(
				"other constraint",
				new SQLException("not null"),
				"some_other_constraint"
			)
		);
		when(crewInviteJpaRepository.saveAndFlush(invite)).thenThrow(exception);
		CrewInviteRepository crewInviteRepository = new JpaCrewInviteRepository(crewInviteJpaRepository);

		assertThatThrownBy(() -> crewInviteRepository.save(invite))
			.isSameAs(exception);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintNameExistsOnlyInMessage() {
		CrewInviteJpaRepository crewInviteJpaRepository = mock(CrewInviteJpaRepository.class);
		CrewInvite invite = CrewInvite.createPending(1L, 10L, 20L);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint [uk_crew_invites_pending_crew_target_user]"
		);
		when(crewInviteJpaRepository.saveAndFlush(invite)).thenThrow(exception);
		CrewInviteRepository crewInviteRepository = new JpaCrewInviteRepository(crewInviteJpaRepository);

		assertThatThrownBy(() -> crewInviteRepository.save(invite))
			.isSameAs(exception);
	}
}
