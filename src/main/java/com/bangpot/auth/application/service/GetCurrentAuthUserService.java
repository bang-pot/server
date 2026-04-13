package com.bangpot.auth.application.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.infrastructure.config.AuthRequiredTermsProperties;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCurrentAuthUserService implements GetCurrentAuthUserUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final AuthRequiredTermsProperties authRequiredTermsProperties;

	@Override
	public View handle(Query query) {
		if (query.userId() == null) {
			return View.guest(authRequiredTermsProperties.getRequiredTermsVersion());
		}
		AuthUser user = authUserRepository.findById(query.userId()).orElse(null);
		if (user == null) {
			return View.guest(authRequiredTermsProperties.getRequiredTermsVersion());
		}

		AuthStatus authStatus = user.requiresCompletion() ? AuthStatus.TEMP : AuthStatus.FULL;
		Instant requiredTermsAcceptedAt = user.getRequiredTermsAgreement() == null
			? null
			: user.getRequiredTermsAgreement().getAcceptedAt();
		String nickname = null;
		if (!user.requiresCompletion()) {
			User profile = userRepository.findById(user.getId())
				.orElseThrow(() -> new UserNotFoundException(user.getId()));
			nickname = profile.getNickname();
		}

		return View.authenticated(
			authStatus,
			user.requiresCompletion(),
			user.getPendingRedirectPath(),
			authRequiredTermsProperties.getRequiredTermsVersion(),
			AuthenticatedUserView.of(user.getId(), nickname),
			requiredTermsAcceptedAt
		);
	}
}
