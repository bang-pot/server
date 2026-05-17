package com.banglog.auth.application.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.auth.application.config.AuthRequiredTermsProperties;
import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.banglog.auth.domain.AuthUser;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCurrentAuthUserService implements GetCurrentAuthUserUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final AuthRequiredTermsProperties authRequiredTermsProperties;

	@Override
	public View handle(Query query) {
		Long userId = query.userId();
		AuthUser user = userId == null
				? null
				: authUserRepository.findById(userId).orElse(null);
		if (user == null) {
			return View.guest();
		}

		boolean isTemp = user.isTemp();
		AuthStatus authStatus = isTemp ? AuthStatus.TEMP : AuthStatus.FULL;
		Instant requiredTermsAcceptedAt = user.getRequiredTermsAgreement() == null
			? null
			: user.getRequiredTermsAgreement().getAcceptedAt();
		String nickname = null;
		if (!isTemp) {
			User profile = userRepository.findById(user.getId())
				.orElseThrow(() -> new IllegalStateException(
					"완료된 회원의 프로필 정보가 없습니다. userId=" + user.getId()
				));
			nickname = profile.getNickname();
		}

		return View.authenticated(
			authStatus,
			isTemp,
			isTemp ? user.getPendingRedirectPath() : null,
			authRequiredTermsProperties.getRequiredTermsVersion(),
			AuthenticatedUserView.of(user.getId(), nickname),
			requiredTermsAcceptedAt
		);
	}
}
