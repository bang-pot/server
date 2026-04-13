package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewInviteNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewInviteCandidatesService implements GetCrewInviteCandidatesUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		Crew crew = crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		completedUserAccessService.validateCompletedUser(query.leaderUserId(), "크루 초대 대상을 조회할 수 없습니다.");

		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), query.leaderUserId())) {
			throw new AccessDeniedException("크루 초대 대상을 조회할 수 없습니다.");
		}
		if (!crew.allowsDirectInvite()) {
			throw new CrewInviteNotAllowedException(crew.getId());
		}

		return userRepository.findCompletedUsersByNicknameContaining(query.nickname()).stream()
			.filter(user -> !user.getId().equals(query.leaderUserId()))
			.filter(user -> !crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), user.getId()))
			.filter(user -> !crewInviteRepository.existsPendingByCrewIdAndTargetUserId(crew.getId(), user.getId()))
			.map(user -> View.of(user.getId(), user.getNickname()))
			.toList();
	}
}
