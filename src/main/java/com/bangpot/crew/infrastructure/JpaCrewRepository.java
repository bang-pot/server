package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;

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
		return crewJpaRepository.save(crew);
	}

	@Override
	public Optional<Crew> findById(Long crewId) {
		return crewJpaRepository.findByIdAndStatus(crewId, CrewStatus.ACTIVE);
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

	@Override
	public List<Crew> findPublicCrews() {
		return crewJpaRepository.findAllByStatusAndVisibilityOrderByIdAsc(CrewStatus.ACTIVE, CrewVisibility.PUBLIC);
	}
}
