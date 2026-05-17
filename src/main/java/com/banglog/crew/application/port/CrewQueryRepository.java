package com.banglog.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.banglog.crew.domain.ExploreCrewSort;
import com.banglog.crew.domain.view.CrewHubView;
import com.banglog.crew.domain.view.CrewInviteCandidateAccessView;
import com.banglog.crew.domain.view.CrewInviteCandidatesView;
import com.banglog.crew.domain.view.CrewJoinView;
import com.banglog.crew.domain.view.CrewMemberAccessView;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.crew.domain.view.CrewPoliciesView;
import com.banglog.crew.domain.view.ExploreCrewCardsView;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.PublicCrewPreviewView;
import com.banglog.user.domain.view.MyWithdrawalCheckView;

public interface CrewQueryRepository {

	Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewJoinView> findCrewJoinViewByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewInviteCandidateAccessView> findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId);

	CrewInviteCandidatesView findCrewInviteCandidatesView(
		Long crewId,
		Long leaderUserId,
		String nickname,
		int page,
		int size
	);

	Optional<CrewMembersView> findCrewMembersViewByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(Long crewId, Long userId);

	MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size);

	long countMyCrewsViewByMemberUserId(Long userId);

	PublicCrewPreviewView findPublicCrewPreviewView(int limit);

	ExploreCrewCardsView findExploreCrewCardsView(String keyword, ExploreCrewSort sort, int page, int size);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	MeetingCreateCrewsView findActiveCrewsByUserId(Long userId);

	List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId);
}
