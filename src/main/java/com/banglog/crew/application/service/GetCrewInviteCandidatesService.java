package com.banglog.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewInviteNotAllowedException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.view.CrewInviteCandidateAccessView;
import com.banglog.crew.domain.view.CrewInviteCandidatesView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewInviteCandidatesService implements GetCrewInviteCandidatesUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewInviteCandidatesView handle(Query query) {
		CrewInviteCandidateAccessView access = crewQueryRepository
			.findCrewInviteCandidateAccessByCrewIdAndUserId(query.crewId(), query.leaderUserId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		completedUserAccessService.validateCompletedUser(
			query.leaderUserId(),
			"완료된 사용자만 크루 초대 대상을 조회할 수 있습니다."
		);

		if (access.myRole() != CrewRole.LEADER) {
			throw new AccessDeniedException("크루장만 초대 대상을 조회할 수 있습니다.");
		}
		if (access.visibility() != CrewVisibility.PRIVATE) {
			throw new CrewInviteNotAllowedException(query.crewId());
		}

		String normalizedNickname = normalizeNickname(query.nickname());
		if (normalizedNickname == null) {
			return CrewInviteCandidatesView.of(
				List.of(),
				CrewInviteCandidatesView.Page.of(query.page(), query.size(), false)
			);
		}
		return crewQueryRepository.findCrewInviteCandidatesView(
			query.crewId(),
			query.leaderUserId(),
			normalizedNickname,
			query.page(),
			query.size()
		);
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null) {
			return null;
		}
		String normalizedNickname = nickname.trim();
		return normalizedNickname.isEmpty() ? null : normalizedNickname;
	}
}
