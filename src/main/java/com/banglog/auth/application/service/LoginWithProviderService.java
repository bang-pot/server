package com.banglog.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.application.usecase.LoginWithProviderUseCase;
import com.banglog.auth.domain.AuthUser;
import com.banglog.auth.infrastructure.logging.AuthAuditLogger;
import com.banglog.auth.infrastructure.RedirectPathSanitizer;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginWithProviderService implements LoginWithProviderUseCase {

	private static final String DEFAULT_NEXT_PATH = "/";
	private static final String COMPLETION_PATH = "/auth/complete";

	private final AuthUserRepository authUserRepository;
	private final AuthAuditLogger authAuditLogger;

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

		Result result;
		if (user.isTemp()) {
			user.updatePendingRedirectPath(sanitizedRedirect);
			authUserRepository.save(user);
			result = Result.temp(user.getId(), COMPLETION_PATH, user.getPendingRedirectPath());
		} else {
			result = Result.full(
				user.getId(),
				sanitizedRedirect == null ? DEFAULT_NEXT_PATH : sanitizedRedirect
			);
		}

		authAuditLogger.loginSucceeded(
			command.provider(),
			result.userId(),
			result.authStatus(),
			result.completionRequired()
		);
		return result;
	}
}
