package com.bangpot.crew.infrastructure;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.exception.CrewInviteAlreadyPendingException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewInviteRepository implements CrewInviteRepository {

	private final CrewInviteJpaRepository crewInviteJpaRepository;

	@Override
	public CrewInvite save(CrewInvite invite) {
		try {
			return crewInviteJpaRepository.saveAndFlush(invite);
		} catch (DataIntegrityViolationException exception) {
			if (isDuplicateCrewInvite(exception)) {
				throw new CrewInviteAlreadyPendingException(invite.getCrewId(), invite.getTargetUserId());
			}
			throw exception;
		}
	}

	@Override
	public boolean existsPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
		return crewInviteJpaRepository.existsByCrewIdAndTargetUserIdAndStatus(
			crewId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}

	@Override
	public Optional<CrewInvite> findPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
		return crewInviteJpaRepository.findByCrewIdAndTargetUserIdAndStatus(
			crewId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}

	@Override
	public Optional<CrewInvite> findPendingByIdAndTargetUserId(Long inviteId, Long targetUserId) {
		return crewInviteJpaRepository.findByIdAndTargetUserIdAndStatus(
			inviteId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}

	@Override
	public Optional<CrewInvite> findPendingByIdAndTargetUserIdForUpdate(Long inviteId, Long targetUserId) {
		return crewInviteJpaRepository.findWithLockByIdAndTargetUserIdAndStatus(
			inviteId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}

	@Override
	public Optional<CrewInvite> findById(Long inviteId) {
		return crewInviteJpaRepository.findById(inviteId);
	}

	private boolean isDuplicateCrewInvite(DataIntegrityViolationException exception) {
		ConstraintViolationException constraintViolationException = findConstraintViolationException(exception);
		return constraintViolationException != null
			&& CrewInvite.PENDING_CREW_TARGET_UNIQUE_CONSTRAINT.equalsIgnoreCase(
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
