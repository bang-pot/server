package com.bangpot.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.crew.domain.view.CrewHubView;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.crew.domain.view.CrewPoliciesView;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

public interface CrewQueryRepository {

	Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewMembersView> findCrewMembersViewByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(Long crewId, Long userId);

	MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size);

	long countMyCrewsViewByMemberUserId(Long userId);

	PublicCrewPreviewView findPublicCrewPreviewView(int limit);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	MeetingCreateCrewsView findMeetingCreateCrewsByMemberUserId(Long userId);

	List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId);
}
