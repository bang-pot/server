package com.bangpot.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.LoginWithProviderUseCase;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.infrastructure.RedirectPathSanitizer;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginWithProviderService implements LoginWithProviderUseCase {

	private static final String DEFAULT_NEXT_PATH = "/";
	private static final String COMPLETION_PATH = "/auth/complete";

	private final AuthUserRepository authUserRepository;

	@Override
	public Result handle(Command command) {
		String sanitizedRedirect = RedirectPathSanitizer.sanitize(command.redirectTo());
		AuthUser user = authUserRepository.findByProviderAndProviderId(
			command.provider(),
			command.providerId()
		)
			.orElseGet(() -> authUserRepository.save(
				AuthUser.createTemp(command.provider(), command.providerId(), sanitizedRedirect)
			));

		if (user.requiresCompletion()) {
			user.updatePendingRedirectPath(sanitizedRedirect);
			authUserRepository.save(user);
			return Result.temp(user.getId(), COMPLETION_PATH, user.getPendingRedirectPath());
		}

		return Result.full(
			user.getId(),
			sanitizedRedirect == null ? DEFAULT_NEXT_PATH : sanitizedRedirect
		);
	}
}
