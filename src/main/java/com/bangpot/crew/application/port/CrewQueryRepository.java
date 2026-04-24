package com.bangpot.crew.application.port;

import java.util.List;

import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

public interface CrewQueryRepository {

	MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size);

	long countMyCrewsViewByMemberUserId(Long userId);

	PublicCrewPreviewView findPublicCrewPreviewView(int limit);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	MeetingCreateCrewsView findMeetingCreateCrewsByMemberUserId(Long userId);

	List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId);
}
