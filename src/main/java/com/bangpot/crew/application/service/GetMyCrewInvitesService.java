package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyCrewInvitesService implements GetMyCrewInvitesUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		AuthUser user = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("초대 목록을 조회할 권한이 없습니다.");
		}

		return crewInviteRepository.findByTargetUserId(query.userId()).stream()
			.map(invite -> View.of(
				invite.getId(),
				invite.getCrewId(),
				crewRepository.findById(invite.getCrewId())
					.orElseThrow(() -> new CrewNotFoundException(invite.getCrewId()))
					.getName(),
				userRepository.findById(invite.getInviterUserId())
					.orElseThrow(() -> new UserNotFoundException(invite.getInviterUserId()))
					.getNickname(),
				invite.getStatus().name()
			))
			.toList();
	}
}
