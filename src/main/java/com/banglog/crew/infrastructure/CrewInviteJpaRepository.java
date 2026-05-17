package com.banglog.crew.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banglog.crew.domain.CrewInvite;
import com.banglog.crew.domain.CrewInviteStatus;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.crew.domain.view.MyCrewInvitesView;

import jakarta.persistence.LockModeType;

interface CrewInviteJpaRepository extends JpaRepository<CrewInvite, Long> {

	boolean existsByCrewIdAndTargetUserIdAndStatus(Long crewId, Long targetUserId, CrewInviteStatus status);

	Optional<CrewInvite> findByCrewIdAndTargetUserIdAndStatus(Long crewId, Long targetUserId, CrewInviteStatus status);

	Optional<CrewInvite> findByIdAndTargetUserIdAndStatus(Long inviteId, Long targetUserId, CrewInviteStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CrewInvite> findWithLockByIdAndTargetUserIdAndStatus(
		Long inviteId,
		Long targetUserId,
		CrewInviteStatus status
	);

	@Query("""
		select new com.banglog.crew.domain.view.MyCrewInvitesView$Item(
			invite.id,
			crew.id,
			crew.name,
			inviter.nickname,
			invite.status
		)
		from CrewInvite invite, Crew crew, UserJpaEntity inviter
		where invite.crewId = crew.id
		  and invite.inviterUserId = inviter.id
		  and invite.targetUserId = :targetUserId
		  and crew.status = :activeCrewStatus
		  and inviter.withdrawnAt is null
		order by invite.id desc
		""")
	Slice<MyCrewInvitesView.Item> findMyCrewInviteItemsByTargetUserId(
		@Param("targetUserId") Long targetUserId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		Pageable pageable
	);
}
