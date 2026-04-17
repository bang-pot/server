package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.ProfileHubReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final ProfileHubReadRepository profileHubReadRepository;

	@Override
	public View handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));
		ProfileHubReadRepository.Counts counts = profileHubReadRepository.loadCounts(query.userId());
		return View.of(
			user.getId(),
			user.getNickname(),
			null,
			counts.createdMeetingsCount(),
			counts.joinedMeetingsCount(),
			counts.myCrewsCount(),
			counts.pendingCrewsCount()
		);
	}
}
