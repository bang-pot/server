package com.bangpot.crew.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCrewQueryRepository implements CrewQueryRepository {

	private final CrewJpaRepository crewJpaRepository;

	@Override
	public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyCrewsView.Item> slice = crewJpaRepository.findMyCrewsViewByMemberUserId(
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
	public List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
		return crewJpaRepository.findWithdrawalBlockingActiveCrewsByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE
		);
	}
}
