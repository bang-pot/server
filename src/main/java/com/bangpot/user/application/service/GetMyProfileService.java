package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

	private final UserRepository userRepository;

	@Override
	public View handle(Query query) {
		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}
		return View.of(user.getId(), user.getNickname());
	}
}
