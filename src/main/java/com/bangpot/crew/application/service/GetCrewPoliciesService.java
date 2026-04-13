package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewPolicyRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.domain.CrewPolicy;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewPoliciesService implements GetCrewPoliciesUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewPolicyRepository crewPolicyRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		completedUserAccessService.validateCompletedUser(query.userId(), "가입한 크루원만 정책을 조회할 수 있습니다.");
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 정책을 조회할 수 있습니다.");
		}

		return crewPolicyRepository.findAllByCrewId(query.crewId()).stream()
			.map(this::toView)
			.toList();
	}

	private View toView(CrewPolicy policy) {
		return View.of(policy.getId(), policy.getTitle(), policy.getContent());
	}
}
