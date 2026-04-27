package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewJoinRequestRepository implements CrewJoinRequestRepository {

	private final CrewJoinRequestJpaRepository crewJoinRequestJpaRepository;

	@Override
	public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
		try {
			return crewJoinRequestJpaRepository.saveAndFlush(crewJoinRequest);
		} catch (DataIntegrityViolationException exception) {
			if (isDuplicatePendingCrewJoinRequest(exception)) {
				throw new CrewJoinRequestAlreadyPendingException(
					crewJoinRequest.getCrewId(),
					crewJoinRequest.getUserId()
				);
			}
			throw exception;
		}
	}

	@Override
	public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
		return crewJoinRequestJpaRepository.existsByCrewIdAndUserIdAndStatus(
			crewId,
			userId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public List<CrewJoinRequest> findByCrewId(Long crewId) {
		return crewJoinRequestJpaRepository.findAllByCrewIdOrderByIdAsc(crewId);
	}

	@Override
	public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
		return crewJoinRequestJpaRepository.findAllByCrewIdAndStatusOrderByIdAsc(
			crewId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
		return crewJoinRequestJpaRepository.findByIdAndCrewIdAndStatus(
			requestId,
			crewId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId) {
		return crewJoinRequestJpaRepository.findWithLockByIdAndCrewIdAndStatus(
			requestId,
			crewId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
		return crewJoinRequestJpaRepository.findByIdAndUserIdAndStatus(
			requestId,
			userId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndUserIdForUpdate(Long requestId, Long userId) {
		return crewJoinRequestJpaRepository.findWithLockByIdAndUserIdAndStatus(
			requestId,
			userId,
			CrewJoinRequestStatus.PENDING
		);
	}

	private boolean isDuplicatePendingCrewJoinRequest(DataIntegrityViolationException exception) {
		ConstraintViolationException constraintViolationException = findConstraintViolationException(exception);
		return constraintViolationException != null
			&& CrewJoinRequest.PENDING_CREW_USER_UNIQUE_CONSTRAINT.equalsIgnoreCase(
				constraintViolationException.getConstraintName()
			);
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
