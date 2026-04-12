package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewPolicyRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewPoliciesService implements GetCrewPoliciesUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewPolicyRepository crewPolicyRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 크루 정책을 조회할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 크루 정책을 조회할 수 있습니다.");
		}

		return crewPolicyRepository.findAllByCrewId(query.crewId()).stream()
			.map(policy -> View.of(policy.getId(), policy.getTitle(), policy.getContent()))
			.toList();
	}
}
