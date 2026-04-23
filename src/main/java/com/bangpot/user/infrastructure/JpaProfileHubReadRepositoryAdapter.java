package com.bangpot.user.infrastructure;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.ProfileHubReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaProfileHubReadRepositoryAdapter implements ProfileHubReadRepository {

	private final JpaProfileHubReadRepository repository;

	@Override
	public Counts loadCounts(Long userId) {
		return Counts.of(
			repository.countCreatedMeetings(userId),
			repository.countJoinedMeetings(userId, JpaProfileHubReadRepository.JOINED_STATUSES),
			repository.countMyCrews(userId, CrewMemberStatus.ACTIVE, CrewStatus.ACTIVE),
			repository.countPendingCrews(userId, CrewJoinRequestStatus.PENDING, CrewStatus.ACTIVE, CrewVisibility.PUBLIC)
		);
	}
}
