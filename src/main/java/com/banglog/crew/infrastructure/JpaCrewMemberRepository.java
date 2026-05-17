package com.banglog.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewMemberStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewMemberRepository implements CrewMemberRepository {

	private final CrewMemberJpaRepository crewMemberJpaRepository;

	@Override
	public CrewMember save(CrewMember crewMember) {
		return crewMemberJpaRepository.save(crewMember);
	}

	@Override
	public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.existsByCrewIdAndUserIdAndStatus(crewId, userId, CrewMemberStatus.ACTIVE);
	}

	@Override
	public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.existsByCrewIdAndUserIdAndRoleAndStatus(
			crewId,
			userId,
			CrewRole.LEADER,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
		return crewMemberJpaRepository.existsByCrewIdAndUserIdNotAndStatus(
			crewId,
			userId,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.findByCrewIdAndUserIdAndStatus(crewId, userId, CrewMemberStatus.ACTIVE);
	}

	@Override
	public Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.findByCrewIdAndUserId(crewId, userId);
	}

	@Override
	public List<CrewMember> findAllByCrewId(Long crewId) {
		return crewMemberJpaRepository.findAllByCrewIdAndStatus(crewId, CrewMemberStatus.ACTIVE);
	}

	@Override
	public List<CrewMember> findAllByUserId(Long userId) {
		return crewMemberJpaRepository.findAllByUserIdAndStatusOrderByCrewIdAsc(userId, CrewMemberStatus.ACTIVE);
	}
}
