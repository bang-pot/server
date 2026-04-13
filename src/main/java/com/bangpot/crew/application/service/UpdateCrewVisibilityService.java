package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCrewVisibilityService implements UpdateCrewVisibilityUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.leaderUserId(), "크루 공개 설정을 변경할 권한이 없습니다.");
		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), command.leaderUserId())) {
			throw new AccessDeniedException("크루 공개 설정을 변경할 권한이 없습니다.");
		}

		crew.changeVisibility(command.targetVisibility());
		crewRepository.save(crew);
		return Result.of(crew.getId(), crew.getVisibility().name());
	}
}
