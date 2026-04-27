package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyCrewsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewRepository implements CrewRepository {

	private final CrewJpaRepository crewJpaRepository;

	@Override
	public boolean existsByName(String name) {
		return crewJpaRepository.existsByName(name);
	}

	@Override
	public Crew save(Crew crew) {
		try {
			return crewJpaRepository.save(crew);
		} catch (DataIntegrityViolationException exception) {
			if (isDuplicateCrewName(exception)) {
				throw new DuplicateCrewNameException(crew.getName());
			}
			throw exception;
		}
	}

	@Override
	public Optional<Crew> findById(Long crewId) {
		return crewJpaRepository.findByIdAndStatus(crewId, CrewStatus.ACTIVE);
	}

	@Override
	public Optional<Crew> findByIdForUpdate(Long crewId) {
		return crewJpaRepository.findByIdAndStatusForUpdate(crewId, CrewStatus.ACTIVE);
	}

	@Override
	public Optional<Crew> findByIdForShare(Long crewId) {
		return crewJpaRepository.findByIdAndStatusForShare(crewId, CrewStatus.ACTIVE);
	}

	@Override
	public List<Crew> findActiveByMemberUserId(Long userId) {
		return crewJpaRepository.findActiveByMemberUserId(userId, CrewMemberStatus.ACTIVE, CrewStatus.ACTIVE);
	}

	@Override
	public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
		Slice<MyCrewsView.Item> slice = crewJpaRepository.findMyCrewsViewByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE,
			CrewRole.LEADER,
			PageRequest.of(page, size)
		);
		return MyCrewsView.of(slice.getContent(), MyCrewsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public long countActiveByMemberUserId(Long userId) {
		return crewJpaRepository.countActiveByMemberUserId(userId, CrewMemberStatus.ACTIVE, CrewStatus.ACTIVE);
	}

	@Override
	public long countPendingPublicByUserId(Long userId) {
		return crewJpaRepository.countPendingPublicByUserId(
			userId,
			CrewJoinRequestStatus.PENDING,
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC
		);
	}

	@Override
	public Optional<Crew> findAnyById(Long crewId) {
		return crewJpaRepository.findById(crewId);
	}

	private boolean isDuplicateCrewName(DataIntegrityViolationException exception) {
		ConstraintViolationException constraintViolationException = findConstraintViolationException(exception);
		return constraintViolationException != null
			&& Crew.NAME_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintViolationException.getConstraintName());
	}

	private ConstraintViolationException findConstraintViolationException(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof ConstraintViolationException constraintViolationException) {
				return constraintViolationException;
			}
			current = current.getCause();
		}
		return null;
	}
}
