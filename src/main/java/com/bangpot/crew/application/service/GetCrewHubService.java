package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewHubService implements GetCrewHubUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		Crew crew = crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("내부 크루 허브는 프로필 보완 완료 사용자만 접근할 수 있습니다.");
		}

		CrewMember crewMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), query.userId())
			.orElseThrow(() -> new AccessDeniedException("가입한 크루원만 내부 크루 허브를 조회할 수 있습니다."));

		return Result.of(
			crew.getId(),
			crew.getName(),
			crew.getDescription(),
			crew.getVisibility().name(),
			crew.getImageUrl(),
			crewMember.getRole(),
			false,
			resolvePendingJoinRequestCount(crewMember.getRole(), crew.getId())
		);
	}

	private Integer resolvePendingJoinRequestCount(CrewRole myRole, Long crewId) {
		if (myRole != CrewRole.LEADER) {
			return null;
		}
		return crewJoinRequestRepository.findPendingByCrewId(crewId).size();
	}
}
