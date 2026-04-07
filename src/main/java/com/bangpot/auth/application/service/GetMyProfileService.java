package com.bangpot.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.GetMyProfileUseCase;
import com.bangpot.auth.domain.AuthUser;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

	private final AuthUserRepository authUserRepository;

	@Override
	public View handle(Query query) {
		AuthUser user = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}
		return View.of(user.getId(), user.getNickname());
	}
}
