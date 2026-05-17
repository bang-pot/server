package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.image.application.usecase.AttachImageUploadUseCase;
import com.bangpot.image.domain.ImageUploadCategory;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCrewService implements CreateCrewUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final AttachImageUploadUseCase attachImageUploadUseCase;

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
			attachCoverImage(command)
		));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), command.userId()));

		return Result.of(crew.getId(), crew.getName(), CrewRole.LEADER);
	}

	private String attachCoverImage(Command command) {
		if (command.imageUploadId() == null) {
			return null;
		}
		return attachImageUploadUseCase.handle(AttachImageUploadUseCase.Command.of(
			command.userId(),
			ImageUploadCategory.CREW_COVER_IMAGE,
			List.of(command.imageUploadId())
		)).urls().get(0);
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
