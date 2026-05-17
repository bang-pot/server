package com.banglog.auth.application.service;

import java.time.Clock;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.auth.application.exception.AuthCompletionNotAllowedException;
import com.banglog.auth.application.exception.AuthUserNotFoundException;
import com.banglog.auth.application.exception.MissingRequiredTermsAgreementException;
import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.application.usecase.CompleteTempUserUseCase;
import com.banglog.auth.domain.AuthUser;
import com.banglog.auth.domain.AuthUserStatus;
import com.banglog.auth.domain.RequiredTermsAgreement;
import com.banglog.auth.application.config.AuthRequiredTermsProperties;
import com.banglog.auth.infrastructure.logging.AuthAuditLogger;
import com.banglog.user.application.exception.DuplicateNicknameException;
import com.banglog.user.application.exception.InvalidNicknameException;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.domain.service.NicknamePolicy;
import com.banglog.user.domain.User;

@Service
@Transactional
@RequiredArgsConstructor
public class CompleteTempUserService implements CompleteTempUserUseCase {

	private static final String DEFAULT_NEXT_PATH = "/";

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final Clock clock;
	private final AuthRequiredTermsProperties authRequiredTermsProperties;
	private final AuthAuditLogger authAuditLogger;

	@Override
	public Result handle(Command command) {
		AuthUser user = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (!user.requiresCompletion()) {
			throw new AuthCompletionNotAllowedException(command.userId());
		}

		String normalizedNickname = NicknamePolicy.normalize(command.nickname());
		if (normalizedNickname == null) {
			throw new InvalidNicknameException();
		}
		if (!command.agreedToRequiredTerms()) {
			throw new MissingRequiredTermsAgreementException();
		}
		if (userRepository.existsByNickname(normalizedNickname)) {
			throw new DuplicateNicknameException(normalizedNickname);
		}

		user.completeProfile(
			RequiredTermsAgreement.of(
				authRequiredTermsProperties.getRequiredTermsVersion(),
				Instant.now(clock)
			)
		);
		String nextPath = user.consumePendingRedirectPathOrDefault(DEFAULT_NEXT_PATH);
		authUserRepository.save(user);
		userRepository.save(User.create(user.getId(), normalizedNickname));
		authAuditLogger.authStateChanged(
			user.getId(),
			AuthUserStatus.TEMP,
			AuthUserStatus.FULL,
			"profile_completed"
		);
		return Result.completed(user.getId(), nextPath);
	}
}
