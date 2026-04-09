package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewJoinRequestsService implements GetCrewJoinRequestsUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		requireLeader(query.crewId(), query.leaderUserId());

		return crewJoinRequestRepository.findByCrewId(query.crewId()).stream()
			.map(request -> View.of(
				request.getId(),
				request.getUserId(),
				authUserRepository.findById(request.getUserId())
					.orElseThrow(() -> new AuthUserNotFoundException(request.getUserId()))
					.getNickname(),
				request.getMessage(),
				request.getStatus().name()
			))
			.toList();
	}

	private void requireLeader(Long crewId, Long userId) {
		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crewId, userId)) {
			throw new AccessDeniedException("가입 신청 관리 권한이 없습니다.");
		}
	}
}
