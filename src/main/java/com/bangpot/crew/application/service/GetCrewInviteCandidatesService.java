package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewInviteNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.domain.Crew;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewInviteCandidatesService implements GetCrewInviteCandidatesUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		Crew crew = crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		AuthUser leader = authUserRepository.findById(query.leaderUserId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.leaderUserId()));

		if (leader.requiresCompletion()) {
			throw new AccessDeniedException("크루 초대 후보를 조회할 권한이 없습니다.");
		}
		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), leader.getId())) {
			throw new AccessDeniedException("크루 초대 후보를 조회할 권한이 없습니다.");
		}
		if (!crew.allowsDirectInvite()) {
			throw new CrewInviteNotAllowedException(crew.getId());
		}

		return authUserRepository.findFullUsersByNicknameContaining(query.nickname()).stream()
			.filter(candidate -> !candidate.getId().equals(leader.getId()))
			.filter(candidate -> !crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), candidate.getId()))
			.filter(candidate -> !crewInviteRepository.existsPendingByCrewIdAndTargetUserId(crew.getId(), candidate.getId()))
			.map(candidate -> View.of(candidate.getId(), candidate.getNickname()))
			.toList();
	}
}
