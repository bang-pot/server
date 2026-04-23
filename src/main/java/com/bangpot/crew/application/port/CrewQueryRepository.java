package com.bangpot.crew.application.port;

import java.util.List;

import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

public interface CrewQueryRepository {

	default MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
		return MyCrewsView.of(List.of(), MyCrewsView.Page.of(page, size, false));
	}
	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	default List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
		return List.of();
	}
}
