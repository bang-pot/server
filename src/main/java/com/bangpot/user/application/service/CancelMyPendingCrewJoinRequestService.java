package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.PendingCrewReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelMyPendingCrewJoinRequestService implements CancelMyPendingCrewJoinRequestUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final PendingCrewReadRepository pendingCrewReadRepository;

	@Override
	public Result handle(Command command) {
		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(command.userId())
			.orElseThrow(() -> new UserNotFoundException(command.userId()));

		PendingCrewReadRepository.CancelResult cancelResult = pendingCrewReadRepository.cancel(
			user.getId(),
			command.joinRequestId()
		);
		return Result.of(cancelResult.joinRequestId(), cancelResult.crewId());
	}
}
