package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import com.banglog.crew.application.exception.CrewJoinRequestNotFoundException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewJoinRequestRepository;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.banglog.crew.domain.CrewJoinRequest;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;

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

		CrewJoinRequest joinRequest = crewJoinRequestRepository.findPendingByIdAndCrewIdForUpdate(
			command.requestId(),
			command.crewId()
		).orElseThrow(() -> new CrewJoinRequestNotFoundException(command.crewId(), command.requestId()));

		Optional<CrewMember> existingMembership = crewMemberRepository.findAnyByCrewIdAndUserId(
			command.crewId(),
			joinRequest.getUserId()
		);

		if (existingMembership.isPresent()) {
			CrewMember crewMember = existingMembership.get();
			if (!crewMember.isActive()) {
				crewMember.reactivateAsMember();
				crewMemberRepository.save(crewMember);
			}
		} else {
			crewMemberRepository.save(CrewMember.createMember(command.crewId(), joinRequest.getUserId()));
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
