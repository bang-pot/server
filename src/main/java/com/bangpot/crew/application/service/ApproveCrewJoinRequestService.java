package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApproveCrewJoinRequestService implements ApproveCrewJoinRequestUseCase {

	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));
		requireLeader(command.crewId(), command.leaderUserId());

		CrewJoinRequest joinRequest = crewJoinRequestRepository.findPendingByIdAndCrewId(
			command.requestId(),
			command.crewId()
		).orElseThrow(() -> new CrewJoinRequestNotFoundException(command.crewId(), command.requestId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(command.crewId(), joinRequest.getUserId())) {
			var existingMembership = crewMemberRepository.findAnyByCrewIdAndUserId(command.crewId(), joinRequest.getUserId());
			if (existingMembership.isPresent()) {
				CrewMember crewMember = existingMembership.get();
				crewMember.reactivateAsMember();
				crewMemberRepository.save(crewMember);
			} else {
				crewMemberRepository.save(CrewMember.createMember(command.crewId(), joinRequest.getUserId()));
			}
		}
		joinRequest.approve();
		crewJoinRequestRepository.save(joinRequest);

		return Result.of(command.crewId(), joinRequest.getId(), joinRequest.getUserId(), CrewRole.MEMBER);
	}

	private void requireLeader(Long crewId, Long userId) {
		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crewId, userId)) {
			throw new AccessDeniedException("가입 신청 관리 권한이 없습니다.");
		}
	}
}
