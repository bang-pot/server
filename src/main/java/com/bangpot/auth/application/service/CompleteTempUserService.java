package com.bangpot.auth.application.service;

import java.time.Clock;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthCompletionNotAllowedException;
import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.exception.MissingRequiredTermsAgreementException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.auth.infrastructure.config.AuthRequiredTermsProperties;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

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

		String normalizedNickname = normalizeNickname(command.nickname());
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
		userRepository.save(User.rehydrate(user.getId(), normalizedNickname, true));
		authAuditLogger.authStateChanged(
			user.getId(),
			AuthUserStatus.TEMP,
			AuthUserStatus.FULL,
			"profile_completed"
		);
		return Result.completed(user.getId(), nextPath);
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null) {
			return null;
		}
		String normalizedNickname = nickname.trim();
		if (normalizedNickname.isEmpty()) {
			return null;
		}
		return normalizedNickname;
	}
}
