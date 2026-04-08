package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCrewService implements CreateCrewUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		AuthUser creator = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (creator.requiresCompletion()) {
			throw new AccessDeniedException("크루 생성 권한이 없습니다.");
		}

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
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), creator.getId()));

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
