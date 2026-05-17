package com.banglog.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.banglog.crew.application.exception.DuplicateCrewNameException;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewVisibility;

class JpaCrewRepositoryTest {

	@Test
	void translatesUniqueNameViolationToDuplicateCrewNameException() {
		CrewJpaRepository crewJpaRepository = mock(CrewJpaRepository.class);
		Crew crew = Crew.create("Same Crew", null, CrewVisibility.PUBLIC, null);
		when(crewJpaRepository.save(crew))
			.thenThrow(new DataIntegrityViolationException(
				"constraint violation",
				new ConstraintViolationException(
					"duplicate crew name",
					new SQLException("duplicate key"),
					Crew.NAME_UNIQUE_CONSTRAINT
				)
			));
		CrewRepository crewRepository = new JpaCrewRepository(crewJpaRepository);

		assertThatThrownBy(() -> crewRepository.save(crew))
			.isInstanceOf(DuplicateCrewNameException.class);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintIsDifferent() {
		CrewJpaRepository crewJpaRepository = mock(CrewJpaRepository.class);
		Crew crew = Crew.create("Same Crew", null, CrewVisibility.PUBLIC, null);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint violation",
			new ConstraintViolationException(
				"other constraint",
				new SQLException("not null"),
				"some_other_constraint"
			)
		);
		when(crewJpaRepository.save(crew)).thenThrow(exception);
		CrewRepository crewRepository = new JpaCrewRepository(crewJpaRepository);

		assertThatThrownBy(() -> crewRepository.save(crew))
			.isSameAs(exception);
	}

	@Test
	void rethrowsDataIntegrityViolationWhenConstraintNameExistsOnlyInMessage() {
		CrewJpaRepository crewJpaRepository = mock(CrewJpaRepository.class);
		Crew crew = Crew.create("Same Crew", null, CrewVisibility.PUBLIC, null);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"constraint [uk_crews_name]"
		);
		when(crewJpaRepository.save(crew)).thenThrow(exception);
		CrewRepository crewRepository = new JpaCrewRepository(crewJpaRepository);

		assertThatThrownBy(() -> crewRepository.save(crew))
			.isSameAs(exception);
	}
}
