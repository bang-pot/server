package com.bangpot.auth.application.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.infrastructure.config.AuthRequiredTermsProperties;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCurrentAuthUserService implements GetCurrentAuthUserUseCase {

	private final AuthUserRepository authUserRepository;
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

		return View.authenticated(
			authStatus,
			user.requiresCompletion(),
			user.getPendingRedirectPath(),
			authRequiredTermsProperties.getRequiredTermsVersion(),
			AuthenticatedUserView.of(user.getId(), user.getNickname()),
			requiredTermsAcceptedAt
		);
	}
}
