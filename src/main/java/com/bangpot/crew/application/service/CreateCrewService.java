package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCrewService implements CreateCrewUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "크루 생성 권한이 없습니다.");

		String normalizedName = normalizeRequired(command.name());
		if (crewRepository.existsByName(normalizedName)) {
			throw new DuplicateCrewNameException(normalizedName);
		}

		Crew crew = crewRepository.save(Crew.create(
			normalizedName,
			normalizeOptional(command.description()),
			command.visibility(),
			normalizeOptional(command.imageUrl())
		));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), command.userId()));

		return Result.of(crew.getId(), crew.getName(), CrewRole.LEADER);
	}

	private String normalizeRequired(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeOptional(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
